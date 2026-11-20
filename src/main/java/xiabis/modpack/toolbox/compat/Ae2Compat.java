package xiabis.modpack.toolbox.compat;

import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import xiabis.modpack.toolbox.XmtMod;

/**
 * Reflective access to Applied Energistics 2's {@code IAEPowerStorage}. AE2 block entities such as the Energy
 * Acceptor accept power through {@code injectAEPower(double, Actionable)}, which is what this class uses to feed
 * an ME network. AE2 stays a soft dependency.
 *
 * <p>The {@code Actionable} constant that means "really apply this" was renamed between AE2 versions
 * ({@code MODIFY} in older builds, {@code MODULATE} since 1.21), so it is looked up by name instead of being
 * hardcoded. Any failure (missing class, renamed method, unexpected API) simply disables AE output; it must never
 * throw, because this runs inside a block entity tick.
 */
public final class Ae2Compat {
    private static final String POWER_STORAGE = "appeng.api.networking.energy.IAEPowerStorage";
    private static final String ACTIONABLE = "appeng.api.config.Actionable";
    /** Names of the "apply" constant across AE2 versions, newest first. */
    private static final String[] APPLY_CONSTANTS = {"MODULATE", "MODIFY"};

    private static boolean resolved;
    private static Method injectAePower;
    private static Class<?> powerStorageType;
    private static Object applyAction;

    private Ae2Compat() {
    }

    public static boolean available() {
        if (!resolved) {
            resolve();
        }
        return injectAePower != null && powerStorageType != null && applyAction != null;
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        ModList modList = ModList.get();
        if (modList == null || !modList.isLoaded("ae2")) {
            return;
        }
        try {
            Class<?> actionable = Class.forName(ACTIONABLE);
            powerStorageType = Class.forName(POWER_STORAGE);
            injectAePower = powerStorageType.getMethod("injectAEPower", double.class, actionable);
            applyAction = findEnumConstant(actionable, APPLY_CONSTANTS);
            if (applyAction == null) {
                throw new IllegalStateException("no apply constant in " + ACTIONABLE);
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            powerStorageType = null;
            injectAePower = null;
            applyAction = null;
            XmtMod.LOGGER.warn("AE2 detected but its power API could not be used, AE output is disabled: {}",
                    exception.toString());
        }
    }

    /** Looks up an enum constant by name, tolerating versions that renamed it. */
    private static Object findEnumConstant(Class<?> enumType, String[] candidates) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null) {
            return null;
        }
        for (String candidate : candidates) {
            for (Object constant : constants) {
                if (constant instanceof Enum<?> value && value.name().equals(candidate)) {
                    return constant;
                }
            }
        }
        return null;
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
        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !powerStorageType.isInstance(blockEntity)) {
                return 0.0D;
            }
            Object accepted = injectAePower.invoke(blockEntity, aeBudget, applyAction);
            return accepted instanceof Double value ? Math.max(0.0D, value) : 0.0D;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            XmtMod.LOGGER.debug("AE2 injection failed: {}", exception.toString());
            return 0.0D;
        }
    }
}
