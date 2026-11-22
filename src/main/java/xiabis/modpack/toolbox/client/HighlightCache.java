package xiabis.modpack.toolbox.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import xiabis.modpack.toolbox.util.ArmorMarkers;
import xiabis.modpack.toolbox.util.ArmorSupport;

/**
 * Keeps track of the blocks the local player wants highlighted. A full scan is spread over several
 * client ticks and refreshed every half second, so 36 marked blocks never cost a frame spike.
 */
public final class HighlightCache {
    public record Hit(BlockPos pos, int color) {
    }

    private record SectionRef(int chunkX, int chunkZ, int sectionY, LevelChunkSection section) {
    }

    private static final int RADIUS = 32;
    private static final int MAX_HITS = 6000;
    private static final int SECTIONS_PER_TICK = 96;
    private static final int CYCLE_TICKS = 10;
    private static final int[] COLORS = {
            0xFF4FC3F7, 0xFFFFB74D, 0xFF81C784, 0xFFE57373, 0xFFBA68C8, 0xFF4DD0E1,
            0xFFFFF176, 0xFF90A4AE, 0xFFA1887F, 0xFFFF8A65, 0xFF9CCC65, 0xFF64B5F6,
    };

    private static Set<Block> marked = Set.of();
    private static final Map<Block, Integer> colors = new HashMap<>();
    private static List<Hit> hits = List.of();
    private static List<SectionRef> queue = List.of();
    private static List<Hit> pending = new ArrayList<>();
    private static int cursor;
    private static boolean scanning;
    private static int cooldown;

    public static List<Hit> hits() {
        return hits;
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            marked = Set.of();
            hits = List.of();
            return;
        }
        Set<Block> blocks = new LinkedHashSet<>();
        for (ItemStack stack : ArmorSupport.highlightStacks(minecraft.player)) {
            blocks.addAll(ArmorMarkers.markedBlocks(stack));
        }
        if (!blocks.equals(marked)) {
            marked = Set.copyOf(blocks);
            colors.clear();
            int index = 0;
            for (Block block : blocks) {
                colors.put(block, COLORS[index % COLORS.length]);
                index++;
            }
            hits = List.of();
            queue = List.of();
            pending = new ArrayList<>();
            scanning = false;
            cooldown = 0;
        }
        if (marked.isEmpty()) {
            hits = List.of();
            return;
        }
        if (scanning) {
            process();
            return;
        }
        if (cooldown-- > 0) {
            return;
        }
        startScan(level);
        process();
    }

    private static void startScan(ClientLevel level) {
        List<SectionRef> sections = new ArrayList<>();
        int chunkRadius = (RADIUS + 15) / 16;
        int originX = minecraftChunkX();
        int originZ = minecraftChunkZ();
        for (int chunkX = originX - chunkRadius; chunkX <= originX + chunkRadius; chunkX++) {
            for (int chunkZ = originZ - chunkRadius; chunkZ <= originZ + chunkRadius; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                LevelChunkSection[] sectionsInChunk = chunk.getSections();
                for (int index = 0; index < sectionsInChunk.length; index++) {
                    LevelChunkSection section = sectionsInChunk[index];
                    if (section == null || section.hasOnlyAir()) {
                        continue;
                    }
                    sections.add(new SectionRef(chunkX, chunkZ, level.getMinSection() + index, section));
                }
            }
        }
        queue = sections;
        pending = new ArrayList<>();
        cursor = 0;
        scanning = true;
    }

    private static int minecraftChunkX() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? 0 : SectionPos.blockToSectionCoord(minecraft.player.getBlockX());
    }

    private static int minecraftChunkZ() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? 0 : SectionPos.blockToSectionCoord(minecraft.player.getBlockZ());
    }

    private static void process() {
        int processed = 0;
        while (cursor < queue.size() && processed < SECTIONS_PER_TICK) {
            SectionRef ref = queue.get(cursor++);
            processed++;
            LevelChunkSection section = ref.section();
            if (!section.maybeHas(state -> marked.contains(state.getBlock()))) {
                continue;
            }
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (!marked.contains(state.getBlock())) {
                            continue;
                        }
                        pending.add(new Hit(new BlockPos(
                                ref.chunkX() * 16 + x,
                                ref.sectionY() * 16 + y,
                                ref.chunkZ() * 16 + z),
                                colors.getOrDefault(state.getBlock(), 0xFFFFFFFF)));
                        if (pending.size() >= MAX_HITS) {
                            finish();
                            return;
                        }
                    }
                }
            }
        }
        if (cursor >= queue.size()) {
            finish();
        }
    }

    private static void finish() {
        hits = List.copyOf(pending);
        pending = new ArrayList<>();
        queue = List.of();
        scanning = false;
        cooldown = CYCLE_TICKS;
        cursor = 0;
    }

    private HighlightCache() {
    }
}
