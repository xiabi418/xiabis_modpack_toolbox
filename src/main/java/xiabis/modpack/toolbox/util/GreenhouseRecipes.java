package xiabis.modpack.toolbox.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import xiabis.modpack.toolbox.XmtMod;

/**
 * Turns a seed, a sapling or a plantable plant item into the items the greenhouse should produce.
 *
 * <p>Plants are resolved in this order: items with a hand written harvest table (mushrooms, cane, bamboo, berries,
 * nether fungi ...), vanilla crops with a dedicated block, blocks whose id mirrors the seed (this covers most
 * modded seeds, including Mystical Agriculture where {@code iron_seeds} belongs to {@code iron_crop}), directly
 * plantable block items, then saplings which yield logs and sticks.</p>
 *
 * <p>Harvests are computed with {@link Block#getDrops}, not by rolling the loot table directly, so blocks that
 * generate their drops in code (Mystical Agriculture's crops, which hand out essence) work as well.</p>
 */
public final class GreenhouseRecipes {
    /** A weighted pile of items one harvest of a hand written plant yields. */
    private record Drop(Item item, int min, int max) {
        ItemStack roll(RandomSource random) {
            return new ItemStack(this.item, this.min + random.nextInt(this.max - this.min + 1));
        }
    }

    private static final Map<Item, Block> VANILLA = Map.of(
            Items.WHEAT_SEEDS, Blocks.WHEAT,
            Items.BEETROOT_SEEDS, Blocks.BEETROOTS,
            Items.CARROT, Blocks.CARROTS,
            Items.POTATO, Blocks.POTATOES,
            Items.MELON_SEEDS, Blocks.MELON,
            Items.PUMPKIN_SEEDS, Blocks.PUMPKIN,
            Items.NETHER_WART, Blocks.NETHER_WART,
            Items.TORCHFLOWER_SEEDS, Blocks.TORCHFLOWER,
            Items.PITCHER_POD, Blocks.PITCHER_PLANT);

    /** Plants that do not have a harvestable crop block, so their produce is listed explicitly. */
    private static final Map<Item, List<Drop>> SPECIAL = Map.ofEntries(
            Map.entry(Items.CRIMSON_FUNGUS, List.of(
                    new Drop(Items.SHROOMLIGHT, 1, 2),
                    new Drop(Items.NETHER_WART_BLOCK, 0, 1),
                    new Drop(Items.CRIMSON_STEM, 1, 2))),
            Map.entry(Items.WARPED_FUNGUS, List.of(
                    new Drop(Items.SHROOMLIGHT, 1, 2),
                    new Drop(Items.WARPED_WART_BLOCK, 0, 1),
                    new Drop(Items.WARPED_STEM, 1, 2))),
            Map.entry(Items.BROWN_MUSHROOM, List.of(new Drop(Items.BROWN_MUSHROOM, 1, 3))),
            Map.entry(Items.RED_MUSHROOM, List.of(new Drop(Items.RED_MUSHROOM, 1, 3))),
            Map.entry(Items.SUGAR_CANE, List.of(new Drop(Items.SUGAR_CANE, 1, 3))),
            Map.entry(Items.BAMBOO, List.of(new Drop(Items.BAMBOO, 1, 3))),
            Map.entry(Items.CACTUS, List.of(new Drop(Items.CACTUS, 1, 2))),
            Map.entry(Items.KELP, List.of(new Drop(Items.KELP, 1, 2))),
            Map.entry(Items.SWEET_BERRIES, List.of(new Drop(Items.SWEET_BERRIES, 1, 3))),
            Map.entry(Items.GLOW_BERRIES, List.of(new Drop(Items.GLOW_BERRIES, 1, 2))),
            Map.entry(Items.COCOA_BEANS, List.of(new Drop(Items.COCOA_BEANS, 1, 3))),
            Map.entry(Items.SEA_PICKLE, List.of(new Drop(Items.SEA_PICKLE, 1, 2))),
            Map.entry(Items.CHORUS_FRUIT, List.of(
                    new Drop(Items.CHORUS_FRUIT, 1, 2),
                    new Drop(Items.POPPED_CHORUS_FRUIT, 0, 1))));

    private GreenhouseRecipes() {
    }

    private static final java.util.Set<ResourceLocation> WARNED = new java.util.HashSet<>();

    private static final Map<Class<?>, Method> DROPS_METHODS = new java.util.HashMap<>();

    public static boolean isPlantable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (SPECIAL.containsKey(item) || VANILLA.containsKey(item)) {
            return true;
        }
        if (item instanceof BlockItem blockItem && isPlantBlock(blockItem.getBlock())) {
            return true;
        }
        return id.getPath().endsWith("_seeds") || id.getPath().endsWith("_sapling") || id.getPath().endsWith("_propagule");
    }

    private static boolean isPlantBlock(Block block) {
        return block instanceof CropBlock || block instanceof StemBlock || block instanceof SaplingBlock
                || block == Blocks.MELON || block == Blocks.PUMPKIN || block == Blocks.NETHER_WART;
    }

    /**
     * @return the items one harvest would produce; empty when the stack is not a plant.
     */
    public static List<ItemStack> produce(ServerLevel level, BlockPos pos, ItemStack seed, RandomSource random) {
        if (!isPlantable(seed)) {
            return List.of();
        }
        Item item = seed.getItem();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();

        List<Drop> special = SPECIAL.get(item);
        if (special != null) {
            List<ItemStack> stacks = new ArrayList<>(special.size());
            for (Drop drop : special) {
                ItemStack stack = drop.roll(random);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            return stacks;
        }

        if (path.endsWith("_sapling") || path.endsWith("_propagule")) {
            return saplingDrops(id.getNamespace(), path, random);
        }

        Block block = VANILLA.get(item);
        if (block == null && item instanceof BlockItem blockItem && isPlantBlock(blockItem.getBlock())) {
            block = blockItem.getBlock();
        }
        if (block == null && path.endsWith("_seeds")) {
            String base = path.substring(0, path.length() - "_seeds".length());
            block = findBlock(id.getNamespace(), base + "_crop");
            if (block == null) {
                block = findBlock(id.getNamespace(), base + "s");
            }
            if (block == null) {
                block = findBlock(id.getNamespace(), base);
            }
        }
        if (block != null) {
            List<ItemStack> harvested = harvest(level, pos, block, random);
            if (!harvested.isEmpty()) {
                return harvested;
            }
        }

        if (WARNED.add(id)) {
            XmtMod.LOGGER.warn("No harvest mapping for greenhouse plant {}, using the item itself", id);
        }
        return List.of(new ItemStack(item));
    }

    private static Block findBlock(String namespace, String path) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
        return block == Blocks.AIR ? null : block;
    }

    /**
     * Harvests the given block once, in its fully grown state.
     *
     * <p>{@link Block#getDrops} is protected in {@code BlockBehaviour}, but modded crops such as Mystical
     * Agriculture override it with code generated drops (essence), so it is invoked reflectively and only the
     * loot table is rolled when that is not possible.</p>
     */
    private static List<ItemStack> harvest(ServerLevel level, BlockPos pos, Block block, RandomSource random) {
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.IRON_HOE))
                .withOptionalParameter(LootContextParams.THIS_ENTITY, null);
        BlockState state = mature(block);
        Method method = dropsMethod(block);
        if (method != null) {
            try {
                @SuppressWarnings("unchecked")
                List<ItemStack> stacks = new ArrayList<>((List<ItemStack>) method.invoke(block, state, builder));
                stacks.removeIf(ItemStack::isEmpty);
                if (!stacks.isEmpty()) {
                    return stacks;
                }
            } catch (ReflectiveOperationException | RuntimeException exception) {
                XmtMod.LOGGER.warn("Could not collect the drops of {}: {}", block, exception.toString());
            }
        }
        return rollLootTable(level, builder, state, random, block);
    }

    /**
     * Finds {@code getDrops(BlockState, LootParams.Builder)} on the block's own class: it is protected in
     * {@code BlockBehaviour}, but public on modded crops that generate their drops in code.
     */
    private static Method dropsMethod(Block block) {
        Class<?> type = block.getClass();
        if (DROPS_METHODS.containsKey(type)) {
            return DROPS_METHODS.get(type);
        }
        Method found = null;
        for (Class<?> current = type; current != null && found == null; current = current.getSuperclass()) {
            try {
                Method candidate = current.getDeclaredMethod("getDrops", BlockState.class, LootParams.Builder.class);
                candidate.setAccessible(true);
                found = candidate;
            } catch (NoSuchMethodException | RuntimeException ignored) {
                // keep walking up
            }
        }
        DROPS_METHODS.put(type, found);
        return found;
    }

    private static List<ItemStack> rollLootTable(ServerLevel level, LootParams.Builder builder, BlockState state,
            RandomSource random, Block block) {
        try {
            LootTable table = level.getServer().reloadableRegistries().getLootTable(block.getLootTable());
            LootParams params = builder.withParameter(LootContextParams.BLOCK_STATE, state)
                    .create(LootContextParamSets.BLOCK);
            List<ItemStack> stacks = new ArrayList<>(table.getRandomItems(params, random));
            stacks.removeIf(ItemStack::isEmpty);
            return stacks;
        } catch (RuntimeException exception) {
            XmtMod.LOGGER.warn("Could not roll the loot table of {}: {}", block, exception.toString());
            return List.of();
        }
    }

    /** The crop's fully grown state, so a single roll yields the harvest rather than the seeds. */
    private static BlockState mature(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.AGE_7)) {
            return state.setValue(BlockStateProperties.AGE_7, 7);
        }
        if (state.hasProperty(BlockStateProperties.AGE_25)) {
            return state.setValue(BlockStateProperties.AGE_25, 25);
        }
        if (state.hasProperty(BlockStateProperties.AGE_15)) {
            return state.setValue(BlockStateProperties.AGE_15, 15);
        }
        if (state.hasProperty(BlockStateProperties.AGE_4)) {
            return state.setValue(BlockStateProperties.AGE_4, 4);
        }
        if (state.hasProperty(BlockStateProperties.AGE_3)) {
            return state.setValue(BlockStateProperties.AGE_3, 3);
        }
        if (state.hasProperty(BlockStateProperties.AGE_2)) {
            return state.setValue(BlockStateProperties.AGE_2, 2);
        }
        if (state.hasProperty(BlockStateProperties.AGE_1)) {
            return state.setValue(BlockStateProperties.AGE_1, 1);
        }
        return state;
    }

    private static List<ItemStack> saplingDrops(String namespace, String path, RandomSource random) {
        String wood = path.endsWith("_sapling")
                ? path.substring(0, path.length() - "_sapling".length())
                : path.substring(0, path.length() - "_propagule".length());
        Item log = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, wood + "_log"));
        if (log == Items.AIR) {
            log = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", wood + "_log"));
        }
        List<ItemStack> drops = new ArrayList<>(2);
        if (log != Items.AIR) {
            drops.add(new ItemStack(log, 1 + random.nextInt(3)));
        } else {
            Item self = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
            drops.add(new ItemStack(self == Items.AIR ? Items.OAK_SAPLING : self));
        }
        int sticks = random.nextInt(3);
        if (sticks > 0) {
            drops.add(new ItemStack(Items.STICK, sticks));
        }
        return drops;
    }
}
