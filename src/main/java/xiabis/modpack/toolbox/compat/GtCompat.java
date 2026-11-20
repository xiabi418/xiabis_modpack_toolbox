package xiabis.modpack.toolbox.compat;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import xiabis.modpack.toolbox.XmtMod;

/**
 * Reflective access to GregTech Modern's {@code IEnergyContainer}. GregTech is only a soft
 * dependency, so everything is looked up lazily and any failure simply disables EU output.
 */
public final class GtCompat {
    private static final String HELPER = "com.gregtechceu.gtceu.api.capability.GTCapabilityHelper";
    /** Smallest GregTech voltage tier (ULV, 8 EU); any machine tier accepts it. */
    private static final long VOLTAGE = 8L;

    private static boolean resolved;
    private static Method getEnergyContainer;
    private static Method acceptEnergyFromNetwork;
    private static Method inputsEnergy;

    private GtCompat() {
    }

    public static boolean available() {
        if (!resolved) {
            resolve();
        }
        return getEnergyContainer != null && acceptEnergyFromNetwork != null;
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        ModList modList = ModList.get();
        if (modList == null || !modList.isLoaded("gtceu")) {
            return;
        }
        try {
            Class<?> helper = Class.forName(HELPER);
            getEnergyContainer = helper.getMethod("getEnergyContainer", Level.class, BlockPos.class, Direction.class);
            Class<?> container = Class.forName("com.gregtechceu.gtceu.api.capability.IEnergyContainer");
            acceptEnergyFromNetwork = container.getMethod("acceptEnergyFromNetwork", Direction.class, long.class, long.class);
            inputsEnergy = container.getMethod("inputsEnergy", Direction.class);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            getEnergyContainer = null;
            acceptEnergyFromNetwork = null;
            inputsEnergy = null;
            XmtMod.LOGGER.warn("GregTech detected but its energy API could not be used, EU output is disabled: {}",
                    exception.toString());
        }
    }

    /**
     * Pushes up to {@code euBudget} EU into the block next to {@code pos} on the given side.
     *
     * @return the amount of EU that was actually accepted
     */
    public static long pushEu(Level level, BlockPos pos, Direction side, long euBudget) {
        if (euBudget <= 0L || !available()) {
            return 0L;
        }
        try {
            Object container = getEnergyContainer.invoke(null, level, pos, side);
            if (container == null) {
                return 0L;
            }
            if (inputsEnergy != null && Boolean.FALSE.equals(inputsEnergy.invoke(container, side))) {
                return 0L;
            }
            long amps = euBudget / VOLTAGE;
            if (amps <= 0L) {
                return 0L;
            }
            Object accepted = acceptEnergyFromNetwork.invoke(container, side, VOLTAGE, amps);
            return accepted instanceof Long value ? Math.max(0L, value) : 0L;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            XmtMod.LOGGER.debug("GregTech energy push failed: {}", exception.toString());
            return 0L;
        }
    }
}
