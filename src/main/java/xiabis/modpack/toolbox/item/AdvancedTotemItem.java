package xiabis.modpack.toolbox.item;

import net.minecraft.world.item.Item;

/** A durable totem that uses the standard death-rescue effects through the mod event hook. */
public final class AdvancedTotemItem extends Item {
    public static final int DURABILITY = 256;

    public AdvancedTotemItem(Properties properties) {
        super(properties);
    }
}
