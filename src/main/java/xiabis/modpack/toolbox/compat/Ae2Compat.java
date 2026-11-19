package xiabis.modpack.toolbox.compat;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

/**
 * Reflective access to Applied Energistics 2's {@code IAEPowerStorage}. AE2 block entities such as
 * the Energy Acceptor accept power through {@code injectAEPower(double, Actionable)}, which is what
 * this class uses to feed a ME network. AE2 stays a soft dependency.
 */
public final class Ae2Compat {
    private static final String POWER_STORAGE = "appeng.api.networking.energy.IAEPowerStorage";
    private static final String ACTIONABLE = "appeng.api.config.Actionable";

    private static boolean resolved;
    private static Method injectAePower;
    private static Class<?> powerStorageType;
    private static Object modify;

    private Ae2Compat() {
    }

    public static boolean available() {
        if (!resolved) {
            resolve();
        }
        return injectAePower != null && powerStorageType != null && modify != null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        if (!ModList.get().isLoaded("ae2")) {
            return;
        }
        try {
            powerStorageType = Class.forName(POWER_STORAGE);
            injectAePower = powerStorageType.getMethod("injectAEPower", double.class, Class.forName(ACTIONABLE));
            modify = Enum.valueOf((Class<? extends Enum>) Class.forName(ACTIONABLE).asSubclass(Enum.class), "MODIFY");
        } catch (ReflectiveOperationException | LinkageError ignored) {
            powerStorageType = null;
            injectAePower = null;
            modify = null;
        }
    }

    /**
     * Injects up to {@code aeBudget} AE into the block next to {@code pos} on the given side.
     *
     * @return the amount of AE that was actually accepted
     */
    public static double pushAe(Level level, BlockPos pos, Direction side, double aeBudget) {
        if (aeBudget <= 0.0D || !available()) {
            return 0.0D;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || !powerStorageType.isInstance(blockEntity)) {
            return 0.0D;
        }
        try {
            Object accepted = injectAePower.invoke(blockEntity, aeBudget, modify);
            return accepted instanceof Double value ? Math.max(0.0D, value) : 0.0D;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return 0.0D;
        }
    }
}
