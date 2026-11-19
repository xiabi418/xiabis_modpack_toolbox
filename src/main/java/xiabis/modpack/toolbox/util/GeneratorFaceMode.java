package xiabis.modpack.toolbox.util;

import net.minecraft.network.chat.Component;

/**
 * Per-face configuration of the omni generator. The order is the one the GUI cycles through with
 * left click (forward) and right click (backward).
 */
public enum GeneratorFaceMode {
    INPUT_FLUID,
    INPUT_ITEM,
    OUTPUT_ENERGY,
    BOTH;

    private static final GeneratorFaceMode[] VALUES = values();

    public boolean acceptsFluid() {
        return this == INPUT_FLUID || this == BOTH;
    }

    public boolean acceptsItems() {
        return this == INPUT_ITEM || this == BOTH;
    }

    public boolean outputsEnergy() {
        return this == OUTPUT_ENERGY || this == BOTH;
    }

    public GeneratorFaceMode next(int direction) {
        int index = (ordinal() + direction) % VALUES.length;
        if (index < 0) {
            index += VALUES.length;
        }
        return VALUES[index];
    }

    public static GeneratorFaceMode byOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return INPUT_FLUID;
        }
        return VALUES[ordinal];
    }

    public Component displayName() {
        return Component.translatable("face.xiabis_modpack_toolbox." + name().toLowerCase(java.util.Locale.ROOT));
    }

    public String translationKey() {
        return "face.xiabis_modpack_toolbox." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
