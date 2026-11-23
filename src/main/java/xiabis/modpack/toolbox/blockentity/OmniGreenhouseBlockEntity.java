package xiabis.modpack.toolbox.blockentity;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import xiabis.modpack.toolbox.menu.OmniGreenhouseMenu;
import xiabis.modpack.toolbox.registry.ModBlockEntities;
import xiabis.modpack.toolbox.registry.ModItems;
import xiabis.modpack.toolbox.util.GreenhouseFaceMode;
import xiabis.modpack.toolbox.util.GreenhouseRecipes;

/**
 * Grows a single seed or sapling: every cycle it drinks 1000 mB of water and 1000 FE and drops the
 * plant's harvest into the nine output slots.
 *
 * <p>Speed cards shave 0.1 s (2 ticks) off the one second cycle; once the cycle would drop below one
 * tick the remaining cards each add one extra harvest. Energy cards raise the energy buffer
 * linearly (64000 FE per card).</p>
 */
public class OmniGreenhouseBlockEntity extends BlockEntity implements MenuProvider {
    public static final int OUTPUT_SLOTS = 9;
    public static final int CARD_SLOTS = 2;
    public static final int CARD_LIMIT = 64;
    public static final int BASE_TANK = 640 * 1000;
    public static final int BASE_CAPACITY = 64_000;
    public static final int BASE_PERIOD = 20;
    public static final int TICKS_PER_SPEED_CARD = 2;
    public static final int SPEED_CARDS_BEFORE_EXTRA = 10;
    public static final int FE_PER_CYCLE = 1_000;
    public static final int MB_PER_CYCLE = 1_000;

    private static final Predicate<FluidStack> WATER = stack -> stack.getFluid().is(FluidTags.WATER);

    private final ItemStackHandler seed = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return GreenhouseRecipes.isPlantable(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler outputs = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** The outside world may only pull harvests out of the greenhouse, never push items in. */
    private final IItemHandler outputsExtractOnly = new IItemHandler() {
        @Override
        public int getSlots() {
            return outputs.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return outputs.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return outputs.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return outputs.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };

    private final ItemStackHandler cards = new ItemStackHandler(CARD_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(ModItems.SPEED_CARD.get());
                case 1 -> stack.is(ModItems.ENERGY_CARD.get());
                default -> false;
            };
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

    private final FluidTank tank = new FluidTank(BASE_TANK, WATER) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final IFluidHandler fillOnly = new IFluidHandler() {
        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return tank.getFluidInTank(index);
        }

        @Override
        public int getTankCapacity(int index) {
            return tank.getTankCapacity(index);
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return tank.isFluidValid(index, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return tank.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    private final GreenhouseEnergyStorage energy = new GreenhouseEnergyStorage();

    private final GreenhouseFaceMode[] faces = {
            GreenhouseFaceMode.OUTPUT_ITEMS,
            GreenhouseFaceMode.INPUT_ENERGY,
            GreenhouseFaceMode.INPUT_FLUID,
            GreenhouseFaceMode.INPUT_FLUID,
            GreenhouseFaceMode.INPUT_FLUID,
            GreenhouseFaceMode.INPUT_FLUID};

    private boolean activeInput = true;
    private boolean activeOutput = true;
    private int progress;

    public OmniGreenhouseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OMNI_GREENHOUSE.get(), pos, state);
    }

    // ------------------------------------------------------------------ state

    public ItemStackHandler seedHandler() {
        return seed;
    }

    public ItemStackHandler outputHandler() {
        return outputs;
    }

    public ItemStackHandler cardHandler() {
        return cards;
    }

    public FluidTank tank() {
        return tank;
    }

    public GreenhouseEnergyStorage energyStorage() {
        return energy;
    }

    public GreenhouseFaceMode faceMode(Direction direction) {
        return faces[direction.get3DDataValue()];
    }

    public void setFaceMode(Direction direction, GreenhouseFaceMode mode) {
        faces[direction.get3DDataValue()] = mode;
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

    public int progress() {
        return progress;
    }

    public int speedCards() {
        return cards.getStackInSlot(0).getCount();
    }

    public int energyCards() {
        return cards.getStackInSlot(1).getCount();
    }

    public int capacity() {
        return BASE_CAPACITY * (1 + energyCards());
    }

    /** Ticks between two harvests; never below one. */
    public int period() {
        return Math.max(1, BASE_PERIOD - TICKS_PER_SPEED_CARD * speedCards());
    }

    /** Extra harvests granted by the speed cards that no longer fit in the one second cycle. */
    public int extraHarvests() {
        return Math.max(0, speedCards() - SPEED_CARDS_BEFORE_EXTRA);
    }

    // ------------------------------------------------------------------ ticking

    public static void serverTick(Level level, BlockPos pos, BlockState state, OmniGreenhouseBlockEntity greenhouse) {
        greenhouse.tick(level, pos);
    }

    private void tick(Level level, BlockPos pos) {
        energy.setCapacity(capacity());

        if (activeInput) {
            pullFromNeighbours(level, pos);
        }

        if (!canRun()) {
            progress = 0;
        } else {
            progress++;
            if (progress >= period()) {
                progress = 0;
                harvest(level, pos);
            }
        }

        if (activeOutput) {
            pushItems(level, pos);
        }
        setChanged();
    }

    private boolean canRun() {
        return !seed.getStackInSlot(0).isEmpty()
                && tank.getFluidAmount() >= MB_PER_CYCLE
                && tank.getFluid().getFluid().is(FluidTags.WATER)
                && energy.getEnergyStored() >= FE_PER_CYCLE;
    }

    private void harvest(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        List<ItemStack> rolled = GreenhouseRecipes.produce(server, pos, seed.getStackInSlot(0), server.getRandom());
        if (rolled.isEmpty()) {
            return;
        }
        energy.extractInternal(FE_PER_CYCLE);
        tank.drain(MB_PER_CYCLE, IFluidHandler.FluidAction.EXECUTE);
        int harvests = 1 + extraHarvests();
        for (int harvest = 0; harvest < harvests; harvest++) {
            List<ItemStack> drops = harvest == 0 ? rolled
                    : GreenhouseRecipes.produce(server, pos, seed.getStackInSlot(0), server.getRandom());
            for (ItemStack stack : drops) {
                insertOrDrop(level, pos, stack);
            }
        }
    }

    private void insertOrDrop(Level level, BlockPos pos, ItemStack stack) {
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(outputs, stack.copy(), false);
        if (!remainder.isEmpty()) {
            Block.popResource(level, pos, remainder);
        }
    }

    private void pullFromNeighbours(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            GreenhouseFaceMode mode = faceMode(direction);
            if (mode.acceptsFluid() && tank.getFluidAmount() < tank.getCapacity()) {
                IFluidHandler source = level.getCapability(Capabilities.FluidHandler.BLOCK,
                        pos.relative(direction), direction.getOpposite());
                if (source != null) {
                    FluidStack drained = source.drain(new FluidStack(Fluids.WATER, tank.getCapacity() - tank.getFluidAmount()),
                            IFluidHandler.FluidAction.SIMULATE);
                    if (!drained.isEmpty() && drained.getFluid().is(FluidTags.WATER)) {
                        int accepted = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                        if (accepted > 0) {
                            source.drain(new FluidStack(drained.getFluid(), accepted), IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
            if (mode.acceptsEnergy() && energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                IEnergyStorage source = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                        pos.relative(direction), direction.getOpposite());
                if (source != null) {
                    int amount = Math.min(energy.getMaxEnergyStored() - energy.getEnergyStored(), 100_000);
                    int extracted = source.extractEnergy(amount, true);
                    if (extracted > 0) {
                        int accepted = energy.receiveEnergy(extracted, false);
                        if (accepted > 0) {
                            source.extractEnergy(accepted, false);
                        }
                    }
                }
            }
        }
    }

    private void pushItems(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!faceMode(direction).outputsItems()) {
                continue;
            }
            IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK,
                    pos.relative(direction), direction.getOpposite());
            if (target == null) {
                continue;
            }
            for (int slot = 0; slot < outputs.getSlots(); slot++) {
                ItemStack stack = outputs.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, stack.copy(), false);
                outputs.setStackInSlot(slot, remainder);
            }
        }
    }

    // ------------------------------------------------------------------ capabilities

    public IItemHandler itemHandler(Direction side) {
        if (side == null || faceMode(side).outputsItems()) {
            return outputsExtractOnly;
        }
        return null;
    }

    public IFluidHandler fluidHandler(Direction side) {
        if (side == null || faceMode(side).acceptsFluid()) {
            return fillOnly;
        }
        return null;
    }

    public net.neoforged.neoforge.energy.IEnergyStorage energyHandler(Direction side) {
        if (side == null || faceMode(side).acceptsEnergy()) {
            return energy;
        }
        return null;
    }

    /** Drops everything the greenhouse holds; called when the block is broken. */
    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < seed.getSlots(); slot++) {
            Block.popResource(level, pos, seed.getStackInSlot(slot));
            seed.setStackInSlot(slot, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < outputs.getSlots(); slot++) {
            Block.popResource(level, pos, outputs.getStackInSlot(slot));
            outputs.setStackInSlot(slot, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < cards.getSlots(); slot++) {
            Block.popResource(level, pos, cards.getStackInSlot(slot));
            cards.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public Component getDisplayName() {
        return Component.translatable("block.xiabis_modpack_toolbox.omni_greenhouse");
    }

    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new OmniGreenhouseMenu(id, inventory, this);
    }

    // ------------------------------------------------------------------ serialisation

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Seed", seed.serializeNBT(registries));
        tag.put("Outputs", outputs.serializeNBT(registries));
        tag.put("Cards", cards.serializeNBT(registries));
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putBoolean("ActiveInput", activeInput);
        tag.putBoolean("ActiveOutput", activeOutput);
        for (Direction direction : Direction.values()) {
            tag.putInt("Face" + direction.get3DDataValue(), faceMode(direction).ordinal());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Seed")) {
            seed.deserializeNBT(registries, tag.getCompound("Seed"));
        }
        if (tag.contains("Outputs")) {
            outputs.deserializeNBT(registries, tag.getCompound("Outputs"));
        }
        if (tag.contains("Cards")) {
            cards.deserializeNBT(registries, tag.getCompound("Cards"));
        }
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
        energy.setStored(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        activeInput = !tag.contains("ActiveInput") || tag.getBoolean("ActiveInput");
        activeOutput = !tag.contains("ActiveOutput") || tag.getBoolean("ActiveOutput");
        for (Direction direction : Direction.values()) {
            if (tag.contains("Face" + direction.get3DDataValue())) {
                faces[direction.get3DDataValue()] =
                        GreenhouseFaceMode.byOrdinal(tag.getInt("Face" + direction.get3DDataValue()));
            }
        }
    }

    /** Energy buffer of the greenhouse: receive only, capacity follows the energy cards. */
    public static class GreenhouseEnergyStorage extends EnergyStorage {
        public GreenhouseEnergyStorage() {
            super(BASE_CAPACITY, BASE_CAPACITY, 0, 0);
        }

        public void setCapacity(int capacity) {
            this.capacity = Math.max(0, capacity);
            if (this.energy > this.capacity) {
                this.energy = this.capacity;
            }
        }

        public void setStored(int stored) {
            this.energy = Math.clamp(stored, 0, this.capacity);
        }

        public int insertInternal(int amount) {
            int accepted = Math.min(amount, this.capacity - this.energy);
            this.energy += accepted;
            return accepted;
        }

        public void extractInternal(int amount) {
            this.energy = Math.max(0, this.energy - amount);
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int accepted = Math.min(toReceive, this.capacity - this.energy);
            if (!simulate) {
                this.energy += accepted;
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return 0;
        }
    }
}
