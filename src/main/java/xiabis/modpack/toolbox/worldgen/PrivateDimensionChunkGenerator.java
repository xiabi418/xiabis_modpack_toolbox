package xiabis.modpack.toolbox.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.server.level.WorldGenRegion;

/**
 * Reuses vanilla noise, biome decoration and structure placement while applying the private
 * dimensions' terrain boundary rules before carving and decoration run.
 */
public final class PrivateDimensionChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<PrivateDimensionChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(NoiseBasedChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings),
                    Mode.CODEC.fieldOf("mode").forGetter(generator -> generator.mode)
            ).apply(instance, PrivateDimensionChunkGenerator::new));

    private static final int OVERWORLD_LAND_Y = 63;
    private static final int DRY_COLUMN = Integer.MIN_VALUE;
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final Mode mode;

    public PrivateDimensionChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, Mode mode) {
        super(biomeSource, settings);
        this.mode = mode;
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
            StructureManager structureManager, ChunkAccess chunk) {
        return super.fillFromNoise(blender, randomState, structureManager, chunk)
                .thenApply(generated -> {
                    normalizeTerrain(generated);
                    return generated;
                });
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager,
            StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving carving) {
        if (mode == Mode.OVERWORLD) {
            return;
        }
        super.applyCarvers(region, seed, randomState, biomeManager, structureManager, chunk, carving);
        normalizeTerrain(chunk);
    }

    private void normalizeTerrain(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        if (mode == Mode.OVERWORLD) {
            normalizeOverworld(chunk, chunkPos, minY, maxY);
            return;
        }

        BlockState fill = mode == Mode.NETHER ? Blocks.NETHERRACK.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= 128 && y < maxY; y++) {
                    set(chunk, chunkPos, x, y, z,
                            y == 0 && mode == Mode.END ? Blocks.BEDROCK.defaultBlockState() : fill);
                }
                if (mode == Mode.NETHER) {
                    for (int y = 129; y < maxY; y++) {
                        BlockPos pos = new BlockPos(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z);
                        if (chunk.getBlockState(pos).is(Blocks.BEDROCK)) {
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        }
                    }
                } else {
                    for (int y = 129; y < maxY; y++) {
                        set(chunk, chunkPos, x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void normalizeOverworld(ChunkAccess chunk, ChunkPos chunkPos, int minY, int maxY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                int floor = waterFloor(y -> chunk.getBlockState(pos.set(worldX, y, worldZ)), minY, top);
                for (int y = minY; y <= Math.max(top, Math.min(OVERWORLD_LAND_Y, maxY - 1)); y++) {
                    pos.set(worldX, y, worldZ);
                    BlockState original = chunk.getBlockState(pos);
                    BlockState normalized = overworldState(original, y, floor);
                    if (original != normalized) {
                        chunk.setBlockState(pos, normalized, false);
                    }
                }
            }
        }
    }

    /** Structures and features querying the noise column see the same terrain as the generated chunk. */
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState randomState) {
        NoiseColumn column = super.getBaseColumn(x, z, height, randomState);
        if (mode == Mode.OVERWORLD) {
            int minY = height.getMinBuildHeight();
            int top = height.getMaxBuildHeight() - 1;
            while (top >= minY && column.getBlock(top).isAir()) {
                top--;
            }
            int floor = waterFloor(column::getBlock, minY, top);
            for (int y = minY; y <= Math.max(top, Math.min(OVERWORLD_LAND_Y, height.getMaxBuildHeight() - 1)); y++) {
                column.setBlock(y, overworldState(column.getBlock(y), y, floor));
            }
        }
        return column;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor height, RandomState randomState) {
        if (mode != Mode.OVERWORLD) {
            return super.getBaseHeight(x, z, type, height, randomState);
        }
        NoiseColumn column = getBaseColumn(x, z, height, randomState);
        var opaque = type.isOpaque();
        for (int y = height.getMaxBuildHeight() - 1; y >= height.getMinBuildHeight(); y--) {
            if (opaque.test(column.getBlock(y))) {
                return y + 1;
            }
        }
        return height.getMinBuildHeight();
    }

    private static int waterFloor(IntFunction<BlockState> block, int minY, int top) {
        // Only water exposed at the top of a noise column is a river/ocean, not a buried aquifer.
        if (top < minY || !block.apply(top).is(Blocks.WATER)) {
            return DRY_COLUMN;
        }
        int floor = top;
        while (floor >= minY && block.apply(floor).is(Blocks.WATER)) {
            floor--;
        }
        return floor;
    }

    private static BlockState overworldState(BlockState original, int y, int floor) {
        if (floor == DRY_COLUMN) {
            return y <= OVERWORLD_LAND_Y ? STONE : AIR;
        }
        // Preserve the original water surface and bathymetry, but close voids beneath the bed.
        return y <= floor && (original.isAir() || !original.getFluidState().isEmpty()) ? STONE : original;
    }

    private static void set(ChunkAccess chunk, ChunkPos chunkPos, int x, int y, int z, BlockState state) {
        chunk.setBlockState(new BlockPos(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z), state, false);
    }

    public enum Mode {
        OVERWORLD, NETHER, END;

        public static final Codec<Mode> CODEC = Codec.STRING.xmap(
                value -> Mode.valueOf(value.toUpperCase(Locale.ROOT)), Mode::name);
    }
}
