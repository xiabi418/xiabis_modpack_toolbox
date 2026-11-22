package xiabis.modpack.toolbox.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Unbreakable;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.item.OmniArmorItem;
import xiabis.modpack.toolbox.registry.ModComponents;

/**
 * Shared omni armor logic: who wears it, the attribute bonuses and the default enchantments.
 */
public final class ArmorSupport {
    public static final double ARMOR_BONUS = 60.0D;
    public static final double TOUGHNESS_BONUS = 60.0D;
    public static final int PROTECTION_LEVEL = 20;
    public static final int RESPIRATION_LEVEL = 20;

    /** Ids of the armor/toughness modifiers. */
    public static final ResourceLocation ARMOR_ID = XmtMod.id("omni_armor_armor");
    public static final ResourceLocation TOUGHNESS_ID = XmtMod.id("omni_armor_toughness");
    private static final ResourceLocation CURIO_ARMOR_ID = XmtMod.id("omni_armor_curio_armor");
    private static final ResourceLocation CURIO_TOUGHNESS_ID = XmtMod.id("omni_armor_curio_toughness");

    private ArmorSupport() {
    }

    public static boolean isArmor(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof OmniArmorItem;
    }

    public static ItemStack helmet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD);
    }

    public static ItemStack curioHead(Player player) {
        return CuriosSupport.head(player);
    }

    public static boolean wornOnHead(LivingEntity entity) {
        return entity != null && isArmor(entity.getItemBySlot(EquipmentSlot.HEAD));
    }

    public static boolean wornInCurio(Player player) {
        return player != null && isArmor(curioHead(player));
    }

    /** Worn in the helmet slot or in the curios head slot. */
    public static boolean worn(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (wornOnHead(entity)) {
            return true;
        }
        return entity instanceof Player player && wornInCurio(player);
    }

    /** Every armor stack that can contribute highlights (worn or held). */
    public static List<ItemStack> highlightStacks(Player player) {
        List<ItemStack> stacks = new ArrayList<>(4);
        if (player == null) {
            return stacks;
        }
        ItemStack helmet = helmet(player);
        if (isArmor(helmet)) {
            stacks.add(helmet);
        }
        ItemStack curio = curioHead(player);
        if (isArmor(curio)) {
            stacks.add(curio);
        }
        if (isArmor(player.getMainHandItem())) {
            stacks.add(player.getMainHandItem());
        }
        if (isArmor(player.getOffhandItem())) {
            stacks.add(player.getOffhandItem());
        }
        return stacks;
    }

    public static ItemStack resolve(Player player, ArmorSlotTarget target) {
        return switch (target) {
            case HELMET -> helmet(player);
            case CURIO_HEAD -> curioHead(player);
            case MAINHAND -> player.getMainHandItem();
            case OFFHAND -> player.getOffhandItem();
        };
    }

    public static void store(Player player, ArmorSlotTarget target, ItemStack stack) {
        switch (target) {
            case HELMET -> player.setItemSlot(EquipmentSlot.HEAD, stack);
            case CURIO_HEAD -> CuriosSupport.setHead(player, stack);
            case MAINHAND -> player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            case OFFHAND -> player.setItemInHand(InteractionHand.OFF_HAND, stack);
        }
    }

    /** The armor the marker menu should edit, if the player has one. */
    public static ArmorSlotTarget resolveTarget(Player player) {
        if (isArmor(helmet(player))) {
            return ArmorSlotTarget.HELMET;
        }
        if (isArmor(curioHead(player))) {
            return ArmorSlotTarget.CURIO_HEAD;
        }
        if (isArmor(player.getMainHandItem())) {
            return ArmorSlotTarget.MAINHAND;
        }
        return ArmorSlotTarget.OFFHAND;
    }

    public static boolean hasArmor(Player player) {
        return isArmor(helmet(player)) || isArmor(curioHead(player))
                || isArmor(player.getMainHandItem()) || isArmor(player.getOffhandItem());
    }

    /**
     * Curios slots do not apply vanilla item attribute modifiers, so when the armor is worn in the
     * curios head slot (and not in the helmet slot, which already grants them through the item) the
     * bonuses are added as transient modifiers.
     */
    public static void applyCurioAttributes(ServerPlayer player, boolean apply) {
        boolean needed = apply && wornInCurio(player) && !wornOnHead(player);
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (needed) {
            if (armor != null) {
                armor.addOrUpdateTransientModifier(
                        new AttributeModifier(CURIO_ARMOR_ID, ARMOR_BONUS, Operation.ADD_VALUE));
            }
            if (toughness != null) {
                toughness.addOrUpdateTransientModifier(
                        new AttributeModifier(CURIO_TOUGHNESS_ID, TOUGHNESS_BONUS, Operation.ADD_VALUE));
            }
        } else {
            if (armor != null) {
                armor.removeModifier(ARMOR_ID);
            }
            if (toughness != null) {
                toughness.removeModifier(CURIO_TOUGHNESS_ID);
            }
        }
    }

    /** Darkness and blindness are neutralized while the armor is worn. */
    public static void clearBlindingEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.BLINDNESS);
    }

    /** Seeds the unbreakable flag and the default enchantments once, when a registry access is available. */
    public static void ensureDefaults(ItemStack stack, Level level) {
        if (!isArmor(stack) || level == null) {
            return;
        }
        boolean seeded = stack.getOrDefault(ModComponents.ARMOR_SEEDED.get(), false);
        if (seeded) {
            return;
        }
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> lookup =
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            mutable.set(lookup.getOrThrow(Enchantments.PROTECTION), PROTECTION_LEVEL);
            mutable.set(lookup.getOrThrow(Enchantments.RESPIRATION), RESPIRATION_LEVEL);
            mutable.set(lookup.getOrThrow(Enchantments.AQUA_AFFINITY), 1);
        });
        stack.set(ModComponents.ARMOR_SEEDED.get(), true);
    }

    public static boolean hasEffect(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        MobEffectInstance instance = player.getEffect(effect);
        return instance != null;
    }
}
