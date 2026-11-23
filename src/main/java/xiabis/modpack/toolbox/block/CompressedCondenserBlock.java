package xiabis.modpack.toolbox.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xiabis.modpack.toolbox.blockentity.CompressedCondenserBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlockEntities;

public final class CompressedCondenserBlock extends BaseEntityBlock {
    private static final MapCodec<CompressedCondenserBlock> CODEC = simpleCodec(CompressedCondenserBlock::new);
    public CompressedCondenserBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return ModBlockEntities.COMPRESSED_CONDENSER.get().create(pos,state); }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CompressedCondenserBlockEntity condenser) player.openMenu(condenser, b -> b.writeBlockPos(pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
        if (!level.isClientSide && !state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof CompressedCondenserBlockEntity condenser) {
            popResource(level, pos, condenser.takeDisk());
            for (int slot = 0; slot < condenser.cards().getSlots(); slot++) {
                popResource(level, pos, condenser.cards().getStackInSlot(slot));
            }
        }
        super.onRemove(state, level, pos, replacement, moved);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return !level.isClientSide && type == ModBlockEntities.COMPRESSED_CONDENSER.get() ? (l,p,s,b) -> CompressedCondenserBlockEntity.serverTick((net.minecraft.server.level.ServerLevel)l,p,s,(CompressedCondenserBlockEntity)b) : null;
    }
}
