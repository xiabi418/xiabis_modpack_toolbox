package xiabis.modpack.toolbox.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Blocks;
import xiabis.modpack.toolbox.item.ToolMode;

/** Builds the vanilla {@code Tool} component data for each {@link ToolMode}. */
public final class ToolComponents {
    /**
     * Fallback speed baked into the component. The real digging speed is driven by
     * {@code PlayerEvent.BreakSpeed}, which makes every block take the same time no matter
     * how hard it is, so this value is only used when no player is involved.
     */
    public static final float MINING_SPEED = 9.0F;

    /** Target time to break a block, in ticks (20 ticks per second). */
    public static final float TARGET_BREAK_TICKS = 6.0F;
    /** Same target expressed in seconds. */
    public static final float TARGET_BREAK_SECONDS = TARGET_BREAK_TICKS / 20.0F;
    /**
     * Vanilla progress per tick is {@code digSpeed / hardness / 30} (when the block is
     * harvestable), so {@code digSpeed = hardness * 30 / TARGET_BREAK_TICKS} yields the
     * requested break time for any block.
     */
    public static final float SPEED_PER_HARDNESS = 30.0F / TARGET_BREAK_TICKS;

    public static Tool build(ToolMode mode) {
        List<Tool.Rule> rules = new ArrayList<>(9);
        // Tier: netherite. Everything netherite cannot harvest would be denied here
        // (that tag is empty in vanilla, so this item effectively harvests every block).
        rules.add(Tool.Rule.deniesDrops(BlockTags.INCORRECT_FOR_NETHERITE_TOOL));
        if (mode.has(ToolMode.PICKAXE)) {
            rules.add(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, MINING_SPEED));
        }
        if (mode.has(ToolMode.SHOVEL)) {
            rules.add(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, MINING_SPEED));
        }
        if (mode.has(ToolMode.AXE)) {
            rules.add(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, MINING_SPEED));
        }
        if (mode.has(ToolMode.HOE)) {
            rules.add(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_HOE, MINING_SPEED));
        }
        if (mode.has(ToolMode.SHEARS)) {
            rules.add(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), MINING_SPEED));
            rules.add(Tool.Rule.minesAndDrops(BlockTags.LEAVES, MINING_SPEED));
            rules.add(Tool.Rule.overrideSpeed(BlockTags.WOOL, MINING_SPEED));
            rules.add(Tool.Rule.overrideSpeed(List.of(Blocks.VINE, Blocks.GLOW_LICHEN), MINING_SPEED));
        }
        if (rules.size() == 1) {
            rules.add(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), MINING_SPEED));
        }
        return new Tool(List.copyOf(rules), MINING_SPEED, 1);
    }

    private ToolComponents() {
    }
}
