package xiabis.modpack.toolbox.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import java.util.List;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import xiabis.modpack.toolbox.blockentity.OmniBatteryBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlockEntities;
import xiabis.modpack.toolbox.registry.ModComponents;
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

    /** Keeps the stored charge on the dropped battery item. */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof OmniBatteryBlockEntity battery) {
            long stored = battery.storedEnergy();
            if (stored > 0L) {
                for (ItemStack stack : drops) {
                    if (stack.is(this.asItem())) {
                        stack.set(ModComponents.STORED_ENERGY.get(), stored);
                    }
                }
            }
        }
        return drops;
    }

    /** Restores the charge recorded on the battery item when it is placed again. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof OmniBatteryBlockEntity battery
                && stack.has(ModComponents.STORED_ENERGY)) {
            battery.setStoredEnergy(stack.getOrDefault(ModComponents.STORED_ENERGY, 0L));
        }
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
