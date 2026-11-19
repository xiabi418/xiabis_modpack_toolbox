package xiabis.modpack.toolbox.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xiabis.modpack.toolbox.registry.ModComponents;

/**
 * Marks every effect the player currently has and, while worn in a Curios charm slot, keeps all
 * marked effects applied with an infinite duration.
 */
public class MagicPoisonBottleItem extends Item implements ICurioItem {
    private static final String TOOLTIP_PREFIX = "tooltip.xiabis_modpack_toolbox.magic_poison_bottle.";
    private static final String MESSAGE_PREFIX = "message.xiabis_modpack_toolbox.magic_poison_bottle.";

    public MagicPoisonBottleItem(Properties properties) {
        super(properties);
    }

    public static MarkedEffects marked(ItemStack stack) {
        return stack.getOrDefault(ModComponents.MARKED_EFFECTS.get(), MarkedEffects.EMPTY);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        MarkedEffects marked = marked(stack);
        MarkedEffects updated = marked;
        for (MobEffectInstance active : player.getActiveEffects()) {
            updated = updated.mark(active.getEffect(), active.getAmplifier() + 1);
        }

        if (updated.equals(marked)) {
            if (marked.isEmpty()) {
                player.displayClientMessage(Component.translatable(MESSAGE_PREFIX + "no_effects"), true);
            } else {
                player.displayClientMessage(Component.translatable(MESSAGE_PREFIX + "already_marked"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        stack.set(ModComponents.MARKED_EFFECTS.get(), updated);
        level.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS,
                0.7F, 1.6F);
        player.displayClientMessage(Component.translatable(MESSAGE_PREFIX + "marked", updated.entries().size()), true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        MarkedEffects marked = marked(stack);
        if (marked.isEmpty()) {
            tooltip.add(Component.translatable(TOOLTIP_PREFIX + "empty").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable(TOOLTIP_PREFIX + "marked").withStyle(ChatFormatting.GOLD));
            for (MarkedEffects.MarkedEffect entry : marked.sorted()) {
                MutableComponent line = Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable(entry.effect().value().getDescriptionId())
                                .withStyle(ChatFormatting.AQUA));
                if (entry.level() > 1) {
                    line.append(CommonComponents.SPACE).append(levelName(entry.level()));
                }
                tooltip.add(line);
            }
        }
        tooltip.add(Component.translatable(TOOLTIP_PREFIX + "hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component levelName(int level) {
        String key = "enchantment.level." + level;
        // Vanilla only ships translations for 1..10; fall back to the plain number above that.
        if (level >= 1 && level <= 10) {
            return Component.translatable(key).withStyle(ChatFormatting.GOLD);
        }
        return Component.literal("" + level).withStyle(ChatFormatting.GOLD);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) {
            return;
        }
        for (MarkedEffects.MarkedEffect entry : marked(stack).entries()) {
            player.addEffect(new MobEffectInstance(entry.effect(), MobEffectInstance.INFINITE_DURATION,
                    entry.level() - 1, false, false, true));
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) {
            return;
        }
        for (MarkedEffects.MarkedEffect entry : marked(stack).entries()) {
            MobEffectInstance current = player.getEffect(entry.effect());
            if (current != null && current.isInfiniteDuration() && current.getAmplifier() == entry.level() - 1) {
                player.removeEffect(entry.effect());
            }
        }
    }
}
