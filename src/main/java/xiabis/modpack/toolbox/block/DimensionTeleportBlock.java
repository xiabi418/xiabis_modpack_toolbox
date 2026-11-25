package xiabis.modpack.toolbox.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.blockentity.ReturnDimensionBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlocks;

/** Sends a player to one of the private dimensions and creates a personal return block. */
public final class DimensionTeleportBlock extends Block {
    private static final MapCodec<DimensionTeleportBlock> CODEC = simpleCodec(DimensionTeleportBlock::new);

    public DimensionTeleportBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel source)) {
            return InteractionResult.PASS;
        }
        String blockPath = BuiltInRegistries.BLOCK.getKey(this).getPath();
        String targetName;
        String targetDimensionPath;
        switch (blockPath) {
            case "overworld_teleport" -> { targetName = "overworld"; targetDimensionPath = "xiabis_own_dim_overworld"; }
            case "nether_teleport" -> { targetName = "nether"; targetDimensionPath = "xiabis_own_dim_nether"; }
            case "end_teleport" -> { targetName = "end"; targetDimensionPath = "xiabis_own_dim_end"; }
            default -> { return InteractionResult.PASS; }
        }
        ResourceKey<Level> targetDimension = dimensionKey(targetDimensionPath);
        ServerLevel destination = serverPlayer.getServer().getLevel(targetDimension);
        if (destination == null) {
            serverPlayer.displayClientMessage(Component.translatable("message.xiabis_modpack_toolbox.dimension_missing"), true);
            return InteractionResult.CONSUME;
        }

        LandingSpot spot = findLanding(destination, serverPlayer.blockPosition());
        if (spot == null || !destination.setBlockAndUpdate(spot.returnBlock(), ModBlocks.RETURN_DIMENSION.get().defaultBlockState())) {
            serverPlayer.displayClientMessage(Component.translatable("message.xiabis_modpack_toolbox.return_spot_failed"), true);
            return InteractionResult.CONSUME;
        }
        if (!(destination.getBlockEntity(spot.returnBlock()) instanceof ReturnDimensionBlockEntity returnBlock)) {
            destination.setBlockAndUpdate(spot.returnBlock(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            serverPlayer.displayClientMessage(Component.translatable("message.xiabis_modpack_toolbox.return_spot_failed"), true);
            return InteractionResult.CONSUME;
        }
        returnBlock.setReturnPoint(serverPlayer, source);
        serverPlayer.teleportTo(destination, spot.feet().getX() + 0.5D, spot.feet().getY(),
                spot.feet().getZ() + 0.5D, serverPlayer.getYRot(), serverPlayer.getXRot());
        serverPlayer.displayClientMessage(Component.translatable("message.xiabis_modpack_toolbox.dimension_entered",
                Component.translatable("dimension.xiabis_modpack_toolbox." + targetName)), true);
        return InteractionResult.CONSUME;
    }

    private static LandingSpot findLanding(ServerLevel level, BlockPos origin) {
        for (int radius = 0; radius <= 16; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos feet = new BlockPos(x, y, z);
                    if (y <= level.getMinBuildHeight() || y + 1 >= level.getMaxBuildHeight()
                            || !clear(level, feet) || !clear(level, feet.above())
                            || !supported(level, feet.below())) {
                        continue;
                    }
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        BlockPos returnPos = feet.relative(direction);
                        if (returnPos.getY() < level.getMinBuildHeight() || returnPos.getY() >= level.getMaxBuildHeight()
                                || !clear(level, returnPos) || !clear(level, returnPos.above())
                                || !supported(level, returnPos.below())) {
                            continue;
                        }
                        return new LandingSpot(feet, returnPos);
                    }
                }
            }
        }
        return null;
    }

    private static boolean clear(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
    }

    private static boolean supported(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    public static ResourceKey<Level> dimensionKey(String path) {
        return ResourceKey.create(Registries.DIMENSION, XmtMod.id(path));
    }

    private record LandingSpot(BlockPos feet, BlockPos returnBlock) {
    }
}
