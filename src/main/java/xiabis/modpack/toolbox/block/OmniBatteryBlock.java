package xiabis.modpack.toolbox.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import xiabis.modpack.toolbox.blockentity.OmniBatteryBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlockEntities;
import xiabis.modpack.toolbox.util.BatteryFaceMode;

/**
 * The giant battery box: an unbounded FE buffer without a GUI. Right clicking a face flips it between input and
 * output, sneak right clicking toggles active I/O for the whole block. Every interaction is announced on the
 * action bar.
 */
public class OmniBatteryBlock extends BaseEntityBlock {
    private static final MapCodec<OmniBatteryBlock> CODEC = simpleCodec(OmniBatteryBlock::new);

    public OmniBatteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OmniBatteryBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.OMNI_BATTERY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof OmniBatteryBlockEntity battery) {
                OmniBatteryBlockEntity.serverTick(tickLevel, pos, tickState, battery);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof OmniBatteryBlockEntity battery)) {
            return InteractionResult.PASS;
        }
        Direction face = hit.getDirection();
        if (player.isShiftKeyDown()) {
            battery.toggleActiveIo();
            player.displayClientMessage(Component.translatable(
                    "message.xiabis_modpack_toolbox.battery.active",
                    Component.translatable(battery.activeIo()
                            ? "gui.xiabis_modpack_toolbox.on"
                            : "gui.xiabis_modpack_toolbox.off")), true);
            return InteractionResult.CONSUME;
        }
        battery.cycle(face);
        BatteryFaceMode mode = battery.mode(face);
        player.displayClientMessage(Component.translatable(
                "message.xiabis_modpack_toolbox.battery.face",
                Component.translatable("battery.xiabis_modpack_toolbox.face." + face.getName()),
                Component.translatable(mode.translationKey()),
                battery.storedEnergy()), true);
        return InteractionResult.CONSUME;
    }
}
