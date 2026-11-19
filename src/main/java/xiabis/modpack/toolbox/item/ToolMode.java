package xiabis.modpack.toolbox.item;

import net.minecraft.network.chat.Component;

public enum ToolMode {
    ALL("all"),
    PICKAXE("pickaxe"),
    SHOVEL("shovel"),
    AXE("axe"),
    HOE("hoe"),
    SHEARS("shears"),
    SHIELD("shield"),
    WRENCH("wrench"),
    STAFF("staff");

    private final String id;

    ToolMode(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    /** True when this state includes the ability of {@code other}. */
    public boolean has(ToolMode other) {
        return this == ALL || this == other;
    }

    /** 0 = fortune-all, 1 = silk-all, 10+ordinal = fortune single, 20+ordinal = silk single. */
    public int customModelData(boolean silk) {
        if (this == ALL) {
            return silk ? 1 : 0;
        }
        return (silk ? 20 : 10) + this.ordinal();
    }

    public Component displayName() {
        return Component.translatable("toolmode.xiabis_modpack_toolbox." + this.id);
    }

    public static ToolMode byOrdinal(int ordinal) {
        ToolMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return ALL;
        }
        return values[ordinal];
    }
}
