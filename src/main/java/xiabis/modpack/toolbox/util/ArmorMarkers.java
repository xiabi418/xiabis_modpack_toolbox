package xiabis.modpack.toolbox.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import xiabis.modpack.toolbox.registry.ModComponents;

/**
 * Helpers for the fixed-size marker slot list stored on an omni armor stack.
 */
public final class ArmorMarkers {
    public static final int SLOTS = 36;

    private ArmorMarkers() {
    }

    public static List<ItemStack> get(ItemStack armor) {
        if (armor.isEmpty()) {
            return normalize(List.of());
        }
        return normalize(armor.getOrDefault(ModComponents.MARKER_SLOTS.get(), List.of()));
    }

    public static void set(ItemStack armor, List<ItemStack> items) {
        armor.set(ModComponents.MARKER_SLOTS.get(), List.copyOf(normalize(items)));
    }

    public static List<ItemStack> normalize(List<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            result.add(i < items.size() && items.get(i) != null ? items.get(i) : ItemStack.EMPTY);
        }
        return result;
    }

    /** The blocks marked on the given armor stack. */
    public static Set<Block> markedBlocks(ItemStack armor) {
        Set<Block> blocks = new LinkedHashSet<>();
        for (ItemStack marker : get(armor)) {
            if (marker.isEmpty() || !(marker.getItem() instanceof BlockItem blockItem)) {
                continue;
            }
            Block block = blockItem.getBlock();
            if (block != net.minecraft.world.level.block.Blocks.AIR) {
                blocks.add(block);
            }
        }
        return blocks;
    }
}
