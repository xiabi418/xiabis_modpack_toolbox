package xiabis.modpack.toolbox.blockentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import xiabis.modpack.toolbox.registry.ModBlockEntities;

public final class OmniFluidTankBlockEntity extends BlockEntity {
    public static final int CAPACITY = 8_000;
    public static final long AE2_AMOUNT = 2_100_000_000L;
    private final LinkedHashMap<Fluid, Integer> fluids = new LinkedHashMap<>();
    private final IFluidHandler handler = new Handler();

    public OmniFluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OMNI_FLUID_TANK.get(), pos, state);
    }

    public IFluidHandler fluidHandler() { return handler; }
    public java.util.Set<Fluid> fluids() { return java.util.Collections.unmodifiableSet(fluids.keySet()); }
    public int amount(Fluid fluid) { return fluids.getOrDefault(fluid, 0); }

    private void changed() { setChanged(); if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag entries = new ListTag();
        for (Map.Entry<Fluid, Integer> entry : fluids.entrySet()) {
            CompoundTag fluid = new CompoundTag();
            fluid.putString("Id", net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(entry.getKey()).toString());
            fluid.putInt("Amount", entry.getValue());
            entries.add(fluid);
        }
        tag.put("Fluids", entries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluids.clear();
        ListTag entries = tag.getList("Fluids", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag fluidTag = entries.getCompound(i);
            Fluid fluid;
            try {
                fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(
                        net.minecraft.resources.ResourceLocation.parse(fluidTag.getString("Id")));
            } catch (RuntimeException ignored) {
                continue;
            }
            int amount = Math.min(CAPACITY, Math.max(0, fluidTag.getInt("Amount")));
            if (fluid != net.minecraft.world.level.material.Fluids.EMPTY && amount > 0) fluids.put(fluid, amount);
        }
    }

    private final class Handler implements IFluidHandler {
        @Override public int getTanks() { return Math.max(1, fluids.size()); }
        @Override public FluidStack getFluidInTank(int tank) {
            if (tank < 0 || tank >= fluids.size()) return FluidStack.EMPTY;
            Fluid fluid = new ArrayList<>(fluids.keySet()).get(tank);
            return new FluidStack(fluid, fluids.get(fluid));
        }
        @Override public int getTankCapacity(int tank) { return CAPACITY; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return !stack.isEmpty(); }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            Fluid fluid = resource.getFluid();
            int current = fluids.getOrDefault(fluid, 0);
            if (current >= CAPACITY) return resource.getAmount();
            int accepted = Math.min(resource.getAmount(), CAPACITY - current);
            if (action.execute() && accepted > 0) { fluids.put(fluid, current + accepted); changed(); }
            return accepted;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;
            Fluid fluid = resource.getFluid();
            int amount = resource.getAmount();
            int current = fluids.getOrDefault(fluid, 0);
            int drained = current >= CAPACITY ? amount : Math.min(amount, current);
            if (action.execute() && current < CAPACITY && drained > 0) {
                int remaining = current - drained;
                if (remaining == 0) fluids.remove(fluid); else fluids.put(fluid, remaining);
                changed();
            }
            return drained <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, drained);
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || fluids.isEmpty()) return FluidStack.EMPTY;
            Fluid fluid = fluids.keySet().iterator().next();
            return drain(new FluidStack(fluid, maxDrain), action);
        }
    }

}
