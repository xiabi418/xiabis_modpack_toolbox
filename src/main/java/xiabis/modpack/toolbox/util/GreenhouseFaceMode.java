package xiabis.modpack.toolbox.util;

import java.util.Locale;

/**
 * What a single face of the greenhouse is allowed to do.
 */
public enum GreenhouseFaceMode {
    INPUT_FLUID,
    INPUT_ENERGY,
    OUTPUT_ITEMS,
    BOTH;

    public boolean acceptsFluid() {
        return this == INPUT_FLUID || this == BOTH;
    }

    public boolean acceptsEnergy() {
        return this == INPUT_ENERGY || this == BOTH;
    }

    public boolean outputsItems() {
        return this == OUTPUT_ITEMS || this == BOTH;
    }

    public GreenhouseFaceMode next(int direction) {
        GreenhouseFaceMode[] values = values();
        int index = (ordinal() + (direction < 0 ? values.length - 1 : 1)) % values.length;
        return values[index];
    }

    public static GreenhouseFaceMode byOrdinal(int ordinal) {
        GreenhouseFaceMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return INPUT_FLUID;
        }
        return values[ordinal];
    }

    public String translationKey() {
        return "greenhouse.xiabis_modpack_toolbox.mode." + name().toLowerCase(Locale.ROOT);
    }

    public String label() {
        return switch (this) {
            case INPUT_FLUID -> "F";
            case INPUT_ENERGY -> "E";
            case OUTPUT_ITEMS -> "I";
            case BOTH -> "B";
        };
    }
}
