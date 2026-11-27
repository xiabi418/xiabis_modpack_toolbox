package xiabis.modpack.toolbox.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiabis.modpack.toolbox.worldgen.PrivateDimensionChunkGenerator;

@Mixin(PlacedFeature.class)
abstract class PlacedFeatureMixin {
    @Shadow
    public abstract Holder<ConfiguredFeature<?, ?>> feature();

    @Inject(method = "placeWithBiomeCheck", at = @At("HEAD"), cancellable = true)
    private void xmt$filterPrivateDimensionFeatures(WorldGenLevel level, ChunkGenerator generator,
            RandomSource random, BlockPos origin, CallbackInfoReturnable<Boolean> callback) {
        if (generator instanceof PrivateDimensionChunkGenerator privateGenerator
                && !privateGenerator.allowsBiomeFeature(feature())) {
            callback.setReturnValue(false);
        }
    }
}
