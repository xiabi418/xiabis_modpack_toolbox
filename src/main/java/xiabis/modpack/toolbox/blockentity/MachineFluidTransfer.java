package xiabis.modpack.toolbox.blockentity;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

final class MachineFluidTransfer {
    private MachineFluidTransfer() { }

    static void pull(IFluidHandler source, FluidTank target, Fluid fluid) {
        int space = target.getCapacity() - target.getFluidAmount();
        if (space <= 0) return;
        FluidStack offer = source.drain(new FluidStack(fluid, space), FluidAction.SIMULATE);
        if (offer.isEmpty() || offer.getFluid() != fluid) return;
        int accepted = target.fill(offer, FluidAction.SIMULATE);
        if (accepted > 0) {
            FluidStack drained = source.drain(offer.copyWithAmount(accepted), FluidAction.EXECUTE);
            target.fill(drained, FluidAction.EXECUTE);
        }
    }
}
