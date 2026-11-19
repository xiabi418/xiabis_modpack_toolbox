package xiabis.modpack.toolbox.util;

import net.minecraft.network.chat.Component;

/**
 * Output energy flavour of the omni generator.
 *
 * <p>The generator always stores Forge Energy internally; the selected type only decides how the
 * stored energy is handed to the neighbours and in which unit the GUI displays it.</p>
 *
 * <p>Conversion: {@code 1 FE = 0.5 AE = 0.25 EU}, i.e. one EU costs 4 FE and one AE costs 2 FE.</p>
 */
public enum GeneratorEnergyType {
    FE(1.0F),
    EU(0.25F),
    AE(0.5F);

    private final float unitsPerFe;

    GeneratorEnergyType(float unitsPerFe) {
        this.unitsPerFe = unitsPerFe;
    }

    /** How many units of this energy type one FE is worth. */
    public float unitsPerFe() {
        return unitsPerFe;
    }

    /** How many FE are needed for one unit of this energy type. */
    public int fePerUnit() {
        return Math.round(1.0F / unitsPerFe);
    }

    public double toUnits(long fe) {
        return fe * (double) unitsPerFe;
    }

    public long fromUnits(double units) {
        return Math.round(units / (double) unitsPerFe);
    }

    public GeneratorEnergyType next(int direction) {
        GeneratorEnergyType[] values = values();
        int index = (ordinal() + direction) % values.length;
        if (index < 0) {
            index += values.length;
        }
        return values[index];
    }

    public static GeneratorEnergyType byOrdinal(int ordinal) {
        GeneratorEnergyType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return FE;
        }
        return values[ordinal];
    }

    public Component displayName() {
        return Component.translatable("energy.xiabis_modpack_toolbox." + name().toLowerCase(java.util.Locale.ROOT));
    }

    public String translationKey() {
        return "energy.xiabis_modpack_toolbox." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
