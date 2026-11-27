package xiabis.modpack.toolbox.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.tags.BiomeTags;
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
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
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
    private static final int MAX_INLAND_POOL_DEPTH = 5;
    private static final int DRY_COLUMN = Integer.MIN_VALUE;
    private static final int CITY_SIDE_CLEARANCE = 4;
    private static final int CITY_ROOF_CLEARANCE = 6;
    private static final int NETHER_BEDROCK_FLOOR_HEIGHT = 5;
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
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
                    normalizeTerrain(generated, randomState);
                    return generated;
                });
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        super.buildSurface(region, structureManager, randomState, chunk);
        if (mode == Mode.OVERWORLD) {
            normalizeTerrain(chunk, randomState);
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager,
            StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving carving) {
        if (mode == Mode.OVERWORLD) {
            clearAncientCitySpace(region, structureManager, chunk);
            return;
        }
        super.applyCarvers(region, seed, randomState, biomeManager, structureManager, chunk, carving);
        normalizeTerrain(chunk, randomState);
    }

    /** The native decoration hook delegates here so biome and structure registries stay shared. */
    public boolean allowsBiomeFeature(Holder<ConfiguredFeature<?, ?>> feature) {
        return mode != Mode.END || !(feature.is(EndFeatures.END_PLATFORM) || feature.is(EndFeatures.END_SPIKE));
    }

    private static void clearAncientCitySpace(WorldGenRegion region, StructureManager structures, ChunkAccess chunk) {
        var registry = region.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var ancientCity = registry.getHolder(BuiltinStructures.ANCIENT_CITY);
        if (ancientCity.isEmpty()) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        var city = ancientCity.get().value();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // Structure references are ready at this stage. Work on this chunk before templates are placed.
        for (var start : structures.startsForStructure(chunkPos, structure -> structure == city)) {
            for (var piece : start.getPieces()) {
                BoundingBox box = piece.getBoundingBox();
                int minX = Math.max(box.minX() - CITY_SIDE_CLEARANCE, chunkPos.getMinBlockX());
                int maxX = Math.min(box.maxX() + CITY_SIDE_CLEARANCE, chunkPos.getMaxBlockX());
                int minZ = Math.max(box.minZ() - CITY_SIDE_CLEARANCE, chunkPos.getMinBlockZ());
                int maxZ = Math.min(box.maxZ() + CITY_SIDE_CLEARANCE, chunkPos.getMaxBlockZ());
                int minY = Math.max(box.minY(), chunk.getMinBuildHeight() + 5);
                int maxY = Math.min(box.maxY() + CITY_ROOF_CLEARANCE, -1);
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            pos.set(x, y, z);
                            if (!chunk.getBlockState(pos).isAir()) {
                                chunk.setBlockState(pos, AIR, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void normalizeTerrain(ChunkAccess chunk, RandomState randomState) {
        ChunkPos chunkPos = chunk.getPos();
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        if (mode == Mode.OVERWORLD) {
            normalizeOverworld(chunk, chunkPos, minY, maxY, randomState);
            normalizeHeightmaps(chunk);
            return;
        }

        boolean nether = mode == Mode.NETHER;
        BlockState fill = nether ? Blocks.NETHERRACK.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                for (int y = minY; y <= 128 && y < maxY; y++) {
                    pos.set(x, y, z);
                    // Keep the vanilla floor gradient when normalizing again after surface rules and carvers.
                    if (nether && y < minY + NETHER_BEDROCK_FLOOR_HEIGHT && chunk.getBlockState(pos).is(Blocks.BEDROCK)) {
                        continue;
                    }
                    chunk.setBlockState(pos, y == (nether ? minY : 0) ? BEDROCK : fill, false);
                }
                for (int y = Math.max(129, minY); y < maxY; y++) {
                    pos.set(x, y, z);
                    if (!nether || chunk.getBlockState(pos).is(Blocks.BEDROCK)) {
                        chunk.setBlockState(pos, AIR, false);
                    }
                }
            }
        }
    }

    private void normalizeOverworld(ChunkAccess chunk, ChunkPos chunkPos, int minY, int maxY, RandomState randomState) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                int floor = waterFloor(y -> chunk.getBlockState(pos.set(worldX, y, worldZ)), minY, top, worldX, worldZ, randomState);
                BlockState surface = floor == DRY_COLUMN ? drySurface(chunk, pos, worldX, worldZ, minY, top) : AIR;
                for (int y = minY; y <= Math.max(top, Math.min(OVERWORLD_LAND_Y, maxY - 1)); y++) {
                    pos.set(worldX, y, worldZ);
                    BlockState original = chunk.getBlockState(pos);
                    BlockState normalized = overworldState(original, y, minY, floor, surface);
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
            int floor = waterFloor(column::getBlock, minY, top, x, z, randomState);
            BlockState surface = floor == DRY_COLUMN ? drySurface(column::getBlock, minY, top) : AIR;
            for (int y = minY; y <= Math.max(top, Math.min(OVERWORLD_LAND_Y, height.getMaxBuildHeight() - 1)); y++) {
                column.setBlock(y, overworldState(column.getBlock(y), y, minY, floor, surface));
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

    private int waterFloor(IntFunction<BlockState> block, int minY, int top, int x, int z, RandomState randomState) {
        // Only consider water exposed at the top of the column.
        if (top < minY || !block.apply(top).is(Blocks.WATER)) {
            return DRY_COLUMN;
        }
        int floor = top;
        while (floor >= minY && block.apply(floor).is(Blocks.WATER)) {
            floor--;
        }
        int inlandFloor = top - MAX_INLAND_POOL_DEPTH;
        if (floor >= inlandFloor) {
            return floor;
        }
        var biome = getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(top), QuartPos.fromBlock(z), randomState.sampler());
        return biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_OCEAN) ? floor : inlandFloor;
    }

    private static BlockState drySurface(ChunkAccess chunk, BlockPos.MutableBlockPos pos, int x, int z, int minY, int top) {
        for (int y = top; y >= Math.max(minY, top - 4); y--) {
            BlockState state = chunk.getBlockState(pos.set(x, y, z));
            if (surfaceMaterial(state)) {
                return state;
            }
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static BlockState drySurface(IntFunction<BlockState> block, int minY, int top) {
        for (int y = top; y >= Math.max(minY, top - 4); y--) {
            BlockState state = block.apply(y);
            if (surfaceMaterial(state)) {
                return state;
            }
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static boolean surfaceMaterial(BlockState state) {
        return state.blocksMotion() && state.getFluidState().isEmpty()
                && !state.is(Blocks.STONE) && !state.is(Blocks.DEEPSLATE) && !state.is(Blocks.BEDROCK)
                && !state.is(Blocks.NETHERRACK) && !state.is(Blocks.END_STONE);
    }

    private static BlockState overworldState(BlockState original, int y, int minY, int floor, BlockState surface) {
        if (y == minY) {
            return BEDROCK;
        }
        if (floor == DRY_COLUMN) {
            if (y > OVERWORLD_LAND_Y) {
                return AIR;
            }
            if (y == OVERWORLD_LAND_Y) {
                return surface;
            }
        } else if (y > floor) {
            // Keep exposed water above the selected bed.
            return original;
        }
        if (original.isAir() || !original.getFluidState().isEmpty()) {
            return y < -8 ? DEEPSLATE : STONE;
        }
        // Preserve solid noise blocks and the bedrock/deepslate placed by surface rules.
        return original;
    }

    private void normalizeHeightmaps(ChunkAccess chunk) {
        if (mode == Mode.OVERWORLD) {
            Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
        }
    }

    public enum Mode {
        OVERWORLD, NETHER, END;

        public static final Codec<Mode> CODEC = Codec.STRING.xmap(
                value -> Mode.valueOf(value.toUpperCase(Locale.ROOT)), Mode::name);
    }
}
