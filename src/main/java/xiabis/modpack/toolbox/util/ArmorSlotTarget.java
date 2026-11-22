package xiabis.modpack.toolbox.util;

/**
 * Where the omni armor stack whose marker slots are being edited lives.
 */
public enum ArmorSlotTarget {
    HELMET,
    CURIO_HEAD,
    MAINHAND,
    OFFHAND;

    public static ArmorSlotTarget byOrdinal(int ordinal) {
        ArmorSlotTarget[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : HELMET;
    }
}
