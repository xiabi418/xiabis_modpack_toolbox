package xiabis.modpack.toolbox.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
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
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minY; y <= 63; y++) {
                        set(chunk, chunkPos, x, y, z, Blocks.STONE.defaultBlockState());
                    }
                    for (int y = 64; y < maxY; y++) {
                        set(chunk, chunkPos, x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
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

    private static void set(ChunkAccess chunk, ChunkPos chunkPos, int x, int y, int z, BlockState state) {
        chunk.setBlockState(new BlockPos(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z), state, false);
    }

    public enum Mode {
        OVERWORLD, NETHER, END;

        public static final Codec<Mode> CODEC = Codec.STRING.xmap(
                value -> Mode.valueOf(value.toUpperCase(Locale.ROOT)), Mode::name);
    }
}
