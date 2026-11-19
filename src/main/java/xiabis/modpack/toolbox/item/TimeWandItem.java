package xiabis.modpack.toolbox.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import xiabis.modpack.toolbox.world.AccelerationDisplay;
import xiabis.modpack.toolbox.world.AccelerationManager;
import xiabis.modpack.toolbox.world.AccelerationSavedData;

/**
 * Time acceleration wand.
 *
 * <p>Sneak + right click a block entity (furnace, hopper, ...) or a randomly ticking block (crops,
 * saplings, ...) to accelerate it. Every click upgrades the multiplier along
 * 4x → 8x → 16x → 64x → 128x → 256x and resets the 60 second timer; 256x is the cap.
 */
public class TimeWandItem extends Item {
    public TimeWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos pos = context.getClickedPos();
        if (!AccelerationManager.canAccelerate(serverLevel, pos)) {
            player.displayClientMessage(
                    Component.translatable("message.xiabis_modpack_toolbox.time_wand.invalid")
                            .withStyle(ChatFormatting.GRAY),
                    true);
            return InteractionResult.FAIL;
        }
        AccelerationSavedData data = AccelerationSavedData.get(serverLevel);
        AccelerationSavedData.Entry current = data.get(pos);
        int tier;
        if (current == null) {
            tier = 0;
        } else if (current.tier() < AccelerationSavedData.MULTIPLIERS.length - 1) {
            tier = current.tier() + 1;
        } else {
            player.displayClientMessage(
                    Component.translatable("message.xiabis_modpack_toolbox.time_wand.max",
                            format(AccelerationSavedData.MULTIPLIERS[AccelerationSavedData.MULTIPLIERS.length - 1]))
                            .withStyle(ChatFormatting.RED),
                    true);
            return InteractionResult.FAIL;
        }
        data.put(pos, new AccelerationSavedData.Entry(tier,
                serverLevel.getGameTime() + AccelerationSavedData.DURATION_TICKS));
        int multiplier = AccelerationSavedData.MULTIPLIERS[tier];
        player.displayClientMessage(
                Component.translatable("message.xiabis_modpack_toolbox.time_wand.applied",
                        format(multiplier), AccelerationSavedData.DURATION_TICKS / 20)
                        .withStyle(ChatFormatting.AQUA),
                true);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F,
                0.8F + tier * 0.15F);
        AccelerationManager.spawnParticles(serverLevel);
        AccelerationDisplay.sync(serverLevel, pos, tier, AccelerationSavedData.DURATION_TICKS / 20, true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.xiabis_modpack_toolbox.time_wand.usage")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.xiabis_modpack_toolbox.time_wand.multipliers")
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private static String format(int multiplier) {
        return multiplier + "x";
    }
}
