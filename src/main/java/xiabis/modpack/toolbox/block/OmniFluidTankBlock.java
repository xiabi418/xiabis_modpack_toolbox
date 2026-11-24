package xiabis.modpack.toolbox.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xiabis.modpack.toolbox.blockentity.OmniFluidTankBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlockEntities;

public final class OmniFluidTankBlock extends BaseEntityBlock {
    private static final MapCodec<OmniFluidTankBlock> CODEC = simpleCodec(OmniFluidTankBlock::new);
    public OmniFluidTankBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new OmniFluidTankBlockEntity(pos, state); }
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity,
            ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (!(level instanceof ServerLevel serverLevel)) return;
        ItemStack drop = new ItemStack(xiabis.modpack.toolbox.registry.ModItems.OMNI_FLUID_TANK_ITEM.get());
        if (blockEntity != null) {
            blockEntity.saveToItem(drop, serverLevel.registryAccess());
        }
        popResource(level, pos, drop);
    }
}
