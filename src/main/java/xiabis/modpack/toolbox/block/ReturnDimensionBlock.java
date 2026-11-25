package xiabis.modpack.toolbox.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xiabis.modpack.toolbox.blockentity.ReturnDimensionBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlockEntities;

/** Generated at each private-dimension landing point and bound to the entering player. */
public final class ReturnDimensionBlock extends BaseEntityBlock {
    private static final MapCodec<ReturnDimensionBlock> CODEC = simpleCodec(ReturnDimensionBlock::new);

    public ReturnDimensionBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReturnDimensionBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)
                || !(level.getBlockEntity(pos) instanceof ReturnDimensionBlockEntity returnBlock)) {
            return InteractionResult.PASS;
        }
        if (returnBlock.returnPlayer(serverPlayer)) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            return InteractionResult.CONSUME;
        }
        serverPlayer.displayClientMessage(Component.translatable("message.xiabis_modpack_toolbox.return_not_yours"), true);
        return InteractionResult.CONSUME;
    }
}
