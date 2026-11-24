package xiabis.modpack.toolbox.blockentity;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

/** Optional AE2 storage-bus view; loaded only when AE2 is installed. */
public final class OmniFluidTankAeStorage implements MEStorage {
    private final OmniFluidTankBlockEntity tank;
    public OmniFluidTankAeStorage(OmniFluidTankBlockEntity tank) { this.tank = tank; }
    @Override public long insert(AEKey key, long amount, Actionable action, IActionSource source) { return 0; }
    @Override public long extract(AEKey key, long amount, Actionable action, IActionSource source) {
        if (!(key instanceof AEFluidKey fluidKey) || amount <= 0) return 0;
        int stored = tank.amount(fluidKey.getFluid());
        return stored >= OmniFluidTankBlockEntity.CAPACITY ? amount : Math.min(amount, stored);
    }
    @Override public void getAvailableStacks(KeyCounter counter) {
        for (Fluid fluid : tank.fluids()) {
            if (tank.amount(fluid) >= OmniFluidTankBlockEntity.CAPACITY) counter.add(AEFluidKey.of(fluid), OmniFluidTankBlockEntity.AE2_AMOUNT);
        }
    }
    @Override public Component getDescription() { return Component.translatable("block.xiabis_modpack_toolbox.omni_fluid_tank"); }
}
