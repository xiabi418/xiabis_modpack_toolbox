package xiabis.modpack.toolbox.util;

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
 * Turns a seed or a sapling into the items the greenhouse should produce.
 *
 * <p>Plants are resolved in this order: vanilla crops with a dedicated block, blocks whose id mirrors the seed
 * (this covers most modded seeds, including Mystical Agriculture where {@code iron_seeds} belongs to
 * {@code iron_crop}), directly plantable block items, then saplings which yield logs and sticks.</p>
 */
public final class GreenhouseRecipes {
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

    private GreenhouseRecipes() {
    }

    private static final java.util.Set<ResourceLocation> WARNED = new java.util.HashSet<>();

    public static boolean isPlantable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (VANILLA.containsKey(item)) {
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
            List<ItemStack> rolled = roll(level, pos, block, random);
            if (!rolled.isEmpty()) {
                return rolled;
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

    private static List<ItemStack> roll(ServerLevel level, BlockPos pos, Block block, RandomSource random) {
        try {
            LootTable table = level.getServer().reloadableRegistries().getLootTable(block.getLootTable());
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, new ItemStack(Items.IRON_HOE))
                    .withParameter(LootContextParams.BLOCK_STATE, mature(block))
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, null)
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
        drops.add(new ItemStack(Items.STICK, random.nextInt(3)));
        return drops;
    }
}
