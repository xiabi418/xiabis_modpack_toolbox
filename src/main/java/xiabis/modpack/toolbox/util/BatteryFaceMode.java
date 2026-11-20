package xiabis.modpack.toolbox.util;

import java.util.Locale;

/** Per side behaviour of the omni battery: either accept energy or hand it out. */
public enum BatteryFaceMode {
    INPUT("input"),
    OUTPUT("output");

    private final String id;

    BatteryFaceMode(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public BatteryFaceMode next() {
        return this == INPUT ? OUTPUT : INPUT;
    }

    public String translationKey() {
        return "battery.xiabis_modpack_toolbox.mode." + this.id;
    }

    public static BatteryFaceMode byOrdinal(int ordinal) {
        BatteryFaceMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : INPUT;
    }

    public static BatteryFaceMode byName(String name) {
        for (BatteryFaceMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return INPUT;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
