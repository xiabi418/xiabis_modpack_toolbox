package xiabis.modpack.toolbox.blockentity;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import xiabis.modpack.toolbox.compat.Ae2Compat;
import xiabis.modpack.toolbox.compat.GtCompat;
import xiabis.modpack.toolbox.menu.OmniGeneratorMenu;
import xiabis.modpack.toolbox.registry.ModBlockEntities;
import xiabis.modpack.toolbox.registry.ModItems;
import xiabis.modpack.toolbox.util.GeneratorEnergyType;
import xiabis.modpack.toolbox.util.GeneratorFaceMode;

/**
 * Burns solid fuel or lava to produce Forge Energy, which can be pushed out as FE, EU (GregTech
 * Modern) or AE (Applied Energistics 2). Every face has its own input/output role.
 */
public class OmniGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int FUEL_SLOTS = 9;
    public static final int SLOT_LIMIT = 1024;
    public static final int FUEL_TICKS = 1600;
    public static final int LAVA_MB_PER_BURN = 1000;
    public static final int BURN_TICKS_PER_BUCKET = 20_000;
    public static final int BASE_CAPACITY = 10_240;
    public static final int BASE_GENERATION = 4_096;
    public static final int TANK_CAPACITY = 1024 * 1000;
    public static final int CARD_LIMIT = 64;

    private static final Predicate<FluidStack> LAVA = stack ->
            !stack.isEmpty() && stack.getFluid().isSame(Fluids.LAVA);

    private final ItemStackHandler fuel = new ItemStackHandler(FUEL_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isFuelItem(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return SLOT_LIMIT;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler cards = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 ? stack.is(ModItems.ENERGY_CARD.get()) : stack.is(ModItems.SPEED_CARD.get());
        }

        @Override
        public int getSlotLimit(int slot) {
            return CARD_LIMIT;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final FluidTank tank = new FluidTank(TANK_CAPACITY, LAVA) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final GeneratorEnergyStorage energy = new GeneratorEnergyStorage();
    private final GeneratorFaceMode[] faces = {
            GeneratorFaceMode.INPUT_FLUID, GeneratorFaceMode.INPUT_ITEM, GeneratorFaceMode.OUTPUT_ENERGY,
            GeneratorFaceMode.BOTH, GeneratorFaceMode.OUTPUT_ENERGY, GeneratorFaceMode.INPUT_FLUID
    };
    private GeneratorEnergyType energyType = GeneratorEnergyType.FE;
    private boolean activeInput = true;
    private boolean activeOutput = true;
    private int burnTime;
    private int burnTotal;

    public OmniGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OMNI_GENERATOR.get(), pos, state);
    }

    // ------------------------------------------------------------------ configuration access

    public ItemStackHandler fuelHandler() {
        return fuel;
    }

    public ItemStackHandler cardHandler() {
        return cards;
    }

    public FluidTank tank() {
        return tank;
    }

    public GeneratorEnergyStorage energyStorage() {
        return energy;
    }

    public GeneratorFaceMode faceMode(Direction direction) {
        return faces[direction.get3DDataValue()];
    }

    public void setFaceMode(Direction direction, GeneratorFaceMode mode) {
        faces[direction.get3DDataValue()] = mode;
        setChanged();
    }

    public GeneratorEnergyType energyType() {
        return energyType;
    }

    public void setEnergyType(GeneratorEnergyType type) {
        energyType = type;
        setChanged();
    }

    public boolean activeInput() {
        return activeInput;
    }

    public void setActiveInput(boolean value) {
        activeInput = value;
        setChanged();
    }

    public boolean activeOutput() {
        return activeOutput;
    }

    public void setActiveOutput(boolean value) {
        activeOutput = value;
        setChanged();
    }

    public int burnTime() {
        return burnTime;
    }

    public int burnTotal() {
        return burnTotal;
    }

    public int cacheCards() {
        return cards.getStackInSlot(0).getCount();
    }

    public int speedCards() {
        return cards.getStackInSlot(1).getCount();
    }

    /** Total FE the internal buffer can hold: 10 240 FE per cache card, linear. */
    public int capacity() {
        return (int) Math.min(Integer.MAX_VALUE, (long) BASE_CAPACITY * (1L + cacheCards()));
    }

    /** FE produced per tick: 4 096 FE/t per speed card, linear. */
    public int generation() {
        return (int) Math.min(Integer.MAX_VALUE, (long) BASE_GENERATION * (1L + speedCards()));
    }

    // ------------------------------------------------------------------ ticking

    public static void serverTick(Level level, BlockPos pos, BlockState state, OmniGeneratorBlockEntity generator) {
        generator.tick(level, pos);
    }

    private void tick(Level level, BlockPos pos) {
        energy.setCapacity(capacity());
        energy.setTransfer(generation());

        if (activeInput) {
            pullFromNeighbours(level, pos);
        }
        if (burnTime <= 0) {
            startBurning();
        }
        if (burnTime > 0) {
            burnTime--;
            energy.insertInternal(generation());
        }
        if (activeOutput) {
            pushEnergy(level, pos);
        }
        setChanged();
    }

    private void startBurning() {
        for (int slot = 0; slot < fuel.getSlots(); slot++) {
            ItemStack stack = fuel.getStackInSlot(slot);
            if (stack.isEmpty() || !isFuelItem(stack)) {
                continue;
            }
            fuel.extractItem(slot, 1, false);
            burnTime = FUEL_TICKS;
            burnTotal = FUEL_TICKS;
            return;
        }
        if (tank.getFluidAmount() >= LAVA_MB_PER_BURN) {
            tank.drain(LAVA_MB_PER_BURN, IFluidHandler.FluidAction.EXECUTE);
            burnTime = BURN_TICKS_PER_BUCKET;
            burnTotal = BURN_TICKS_PER_BUCKET;
        }
    }

    private void pullFromNeighbours(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            GeneratorFaceMode mode = faceMode(direction);
            BlockPos neighbour = pos.relative(direction);
            Direction opposite = direction.getOpposite();
            if (mode.acceptsItems()) {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbour, opposite);
                if (handler != null) {
                    pullItems(handler);
                }
            }
            if (mode.acceptsFluid() && tank.getFluidAmount() < tank.getCapacity()) {
                IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighbour, opposite);
                if (handler != null) {
                    pullFluid(handler);
                }
            }
        }
    }

    private void pullItems(IItemHandler source) {
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack stack = source.extractItem(slot, SLOT_LIMIT, true);
            if (stack.isEmpty() || !isFuelItem(stack)) {
                continue;
            }
            ItemStack remaining = ItemHandlerHelper.insertItem(fuel, stack, true);
            int movable = stack.getCount() - remaining.getCount();
            if (movable <= 0) {
                continue;
            }
            ItemStack extracted = source.extractItem(slot, movable, false);
            ItemHandlerHelper.insertItem(fuel, extracted, false);
        }
    }

    private void pullFluid(IFluidHandler source) {
        FluidStack drained = source.drain(tank.getCapacity() - tank.getFluidAmount(),
                IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty() || !LAVA.test(drained)) {
            return;
        }
        int accepted = tank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return;
        }
        FluidStack effective = source.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
        tank.fill(effective, IFluidHandler.FluidAction.EXECUTE);
    }

    private void pushEnergy(Level level, BlockPos pos) {
        int budget = Math.min(energy.getEnergyStored(), generation());
        if (budget <= 0) {
            return;
        }
        GeneratorEnergyType type = energyType;
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                return;
            }
            if (!faceMode(direction).outputsEnergy()) {
                continue;
            }
            BlockPos neighbour = pos.relative(direction);
            Direction opposite = direction.getOpposite();
            switch (type) {
                case FE -> budget -= pushFe(level, neighbour, opposite, budget);
                case EU -> budget -= pushEu(level, neighbour, opposite, budget);
                case AE -> budget -= pushAe(level, neighbour, opposite, budget);
            }
        }
    }

    private int pushFe(Level level, BlockPos pos, Direction side, int budget) {
        net.neoforged.neoforge.energy.IEnergyStorage storage =
                level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
        if (storage == null) {
            return 0;
        }
        int accepted = storage.receiveEnergy(budget, false);
        if (accepted > 0) {
            energy.extractInternal(accepted);
        }
        return accepted;
    }

    private int pushEu(Level level, BlockPos pos, Direction side, int budget) {
        long euBudget = budget / 4L;
        long acceptedEu = GtCompat.pushEu(level, pos, side, euBudget);
        if (acceptedEu <= 0L) {
            return 0;
        }
        int spent = (int) Math.min(budget, acceptedEu * 4L);
        energy.extractInternal(spent);
        return spent;
    }

    private int pushAe(Level level, BlockPos pos, Direction side, int budget) {
        double acBudget = budget / 2.0D;
        double acceptedAe = Ae2Compat.pushAe(level, pos, side, acBudget);
        if (acceptedAe > 0.0D) {
            int spent = (int) Math.min(budget, Math.round(acceptedAe * 2.0D));
            energy.extractInternal(spent);
            return spent;
        }
        return 0;
    }

    public static boolean isFuelItem(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
    }

    // ------------------------------------------------------------------ capabilities

    public IItemHandler itemHandler(Direction side) {
        if (side == null) {
            return new RangedWrapper(fuel, 0, FUEL_SLOTS);
        }
        GeneratorFaceMode mode = faceMode(side);
        if (mode.acceptsItems()) {
            return new RangedWrapper(fuel, 0, FUEL_SLOTS);
        }
        return null;
    }

    public IFluidHandler fluidHandler(Direction side) {
        if (side == null || faceMode(side).acceptsFluid()) {
            return tank;
        }
        return null;
    }

    public net.neoforged.neoforge.energy.IEnergyStorage energyHandler(Direction side) {
        if (side == null) {
            return energy;
        }
        GeneratorFaceMode mode = faceMode(side);
        if (mode.outputsEnergy()) {
            return energy;
        }
        if (mode.acceptsItems() || mode.acceptsFluid()) {
            return null;
        }
        return null;
    }

    /** Drops everything the generator holds; called when the block is broken. */
    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < fuel.getSlots(); slot++) {
            Block.popResource(level, pos, fuel.getStackInSlot(slot));
            fuel.setStackInSlot(slot, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < cards.getSlots(); slot++) {
            Block.popResource(level, pos, cards.getStackInSlot(slot));
            cards.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public Component statusComponent() {
        return Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.status",
                energy.getEnergyStored(), capacity(), tank.getFluidAmount() / 1000);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.xiabis_modpack_toolbox.omni_generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new OmniGeneratorMenu(id, inventory, this);
    }

    // ------------------------------------------------------------------ persistence

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Fuel", fuel.serializeNBT(registries));
        tag.put("Cards", cards.serializeNBT(registries));
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTotal", burnTotal);
        tag.putInt("EnergyType", energyType.ordinal());
        tag.putBoolean("ActiveInput", activeInput);
        tag.putBoolean("ActiveOutput", activeOutput);
        for (Direction direction : Direction.values()) {
            tag.putInt("Face" + direction.get3DDataValue(), faceMode(direction).ordinal());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Fuel")) {
            fuel.deserializeNBT(registries, tag.getCompound("Fuel"));
        }
        if (tag.contains("Cards")) {
            cards.deserializeNBT(registries, tag.getCompound("Cards"));
        }
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
        energy.setStored(tag.getInt("Energy"));
        burnTime = tag.getInt("BurnTime");
        burnTotal = tag.getInt("BurnTotal");
        energyType = GeneratorEnergyType.byOrdinal(tag.getInt("EnergyType"));
        activeInput = !tag.contains("ActiveInput") || tag.getBoolean("ActiveInput");
        activeOutput = !tag.contains("ActiveOutput") || tag.getBoolean("ActiveOutput");
        for (Direction direction : Direction.values()) {
            setFaceMode(direction, GeneratorFaceMode.byOrdinal(tag.getInt("Face" + direction.get3DDataValue())));
        }
    }

    /** Energy buffer whose capacity and transfer rate follow the installed cards. */
    public static class GeneratorEnergyStorage extends EnergyStorage {
        public GeneratorEnergyStorage() {
            super(BASE_CAPACITY, BASE_GENERATION, BASE_GENERATION);
        }

        public void setCapacity(int capacity) {
            this.capacity = Math.max(0, capacity);
            if (energy > this.capacity) {
                energy = this.capacity;
            }
        }

        public void setTransfer(int transfer) {
            this.maxReceive = 0;
            this.maxExtract = Math.max(0, transfer);
        }

        public void setStored(int stored) {
            energy = Math.max(0, Math.min(stored, capacity));
        }

        public int insertInternal(int amount) {
            int accepted = Math.min(amount, capacity - energy);
            if (accepted > 0) {
                energy += accepted;
            }
            return accepted;
        }

        public int extractInternal(int amount) {
            int extracted = Math.min(amount, energy);
            if (extracted > 0) {
                energy -= extracted;
            }
            return extracted;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
