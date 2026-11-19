package xiabis.modpack.toolbox.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.Nullable;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.compat.ApotheosisCompat;
import xiabis.modpack.toolbox.registry.ModComponents;
import xiabis.modpack.toolbox.event.PlayerAttributesHandler;
import xiabis.modpack.toolbox.util.ToolComponents;

/**
 * Universal weapon. Extends {@link MaceItem} so the whole falling smash attack
 * (damage bonus, ground/air sound, particles, area knockback, fall damage reset)
 * is the vanilla mace implementation instead of a copy of it.
 */
public class OmniWeaponItem extends MaceItem {
    public static final int DURABILITY = 32_500;
    public static final double ATTACK_SPEED = 418.0D;
    public static final int DEFAULT_LOOTING = 15;
    /** Vanilla bow reaches full power after 20 ticks; quick charge shortens it. */
    public static final int BASE_CHARGE_TICKS = 20;

    private static final ResourceLocation DAMAGE_ID = XmtMod.id("omni_weapon_attack_damage");
    private static final ResourceLocation SPEED_ID = XmtMod.id("omni_weapon_attack_speed");

    public OmniWeaponItem(Properties properties) {
        super(properties);
    }

    /** Loader / vanilla entries that are not real "installed mods". */
    private static final java.util.Set<String> BUILTIN_MODS =
            java.util.Set.of("minecraft", "neoforge", "java", "fml");

    /** Attack damage equals the amount of installed mods (1 mod = 1 damage point). */
    public static double attackDamage() {
        return modCount();
    }

    public static int modCount() {
        ModList modList = ModList.get();
        if (modList == null) {
            return 1;
        }
        long mods = modList.getMods().stream()
                .filter(info -> !BUILTIN_MODS.contains(info.getModId()))
                .count();
        return (int) Math.max(1L, mods);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) {
            return;
        }
        // The weapon is netherite tier and mines like the omni tool. Backfilled here as
        // well so weapons created before this component existed pick it up.
        if (!stack.has(DataComponents.TOOL)) {
            stack.set(DataComponents.TOOL, ToolComponents.build(ToolMode.ALL));
        }
        if (stack.getOrDefault(ModComponents.STATE_APPLIED, false)) {
            return;
        }
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        seedEnchantments(stack, level.registryAccess());
        ApotheosisCompat.applyDefaults(stack);
        stack.set(ModComponents.STATE_APPLIED, true);
    }

    private static void seedEnchantments(ItemStack stack, RegistryAccess access) {
        Optional<HolderLookup.RegistryLookup<Enchantment>> lookup = access.lookup(Registries.ENCHANTMENT);
        if (lookup.isEmpty()) {
            return;
        }
        Optional<Holder.Reference<Enchantment>> looting = lookup.get().get(Enchantments.LOOTING);
        if (!stack.has(DataComponents.ENCHANTMENTS)) {
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        looting.ifPresent(holder -> EnchantmentHelper.updateEnchantments(stack,
                mutable -> mutable.set(holder, DEFAULT_LOOTING)));
    }

    /** Same mining fallback as the omni tool so the weapon keeps working as a tool. */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float speed = super.getDestroySpeed(stack, state);
        if (speed > 1.0F) {
            return speed;
        }
        return ToolComponents.MINING_SPEED;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.xiabis_modpack_toolbox.mining_tier")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.xiabis_modpack_toolbox.mining_speed",
                        String.format("%.2f", ToolComponents.TARGET_BREAK_SECONDS),
                        (int) ToolComponents.TARGET_BREAK_TICKS)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.xiabis_modpack_toolbox.omni_weapon.reach",
                        (int) PlayerAttributesHandler.WEAPON_ENTITY_RANGE_BONUS)
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(DAMAGE_ID, Math.max(0.0D, attackDamage() - 1.0D),
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(SPEED_ID, ATTACK_SPEED - 4.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        if (ability == ItemAbilities.SWORD_DIG || ability == ItemAbilities.SWORD_SWEEP) {
            return true;
        }
        if (ability == ItemAbilities.AXE_DIG || ability == ItemAbilities.AXE_STRIP
                || ability == ItemAbilities.AXE_SCRAPE || ability == ItemAbilities.AXE_WAX_OFF) {
            return true;
        }
        return super.canPerformAction(stack, ability);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    /** Right click draws like a bow when the player has ammo. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (findAmmo(player).isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72_000;
    }

    /** Ticks needed for a full draw, shortened by quick charge (like a crossbow). */
    public static int fullChargeTicks(ItemStack stack, @Nullable Level level) {
        int quickCharge = enchantLevel(stack, level, Enchantments.QUICK_CHARGE);
        return Math.max(3, BASE_CHARGE_TICKS - 5 * quickCharge);
    }

    /**
     * Reads an enchantment level straight off the stack, so it also works on the client where no
     * level (and therefore no registry access) is available while the item is being rendered.
     */
    private static int enchantLevel(ItemStack stack, @Nullable Level level, ResourceKey<Enchantment> key) {
        if (level != null) {
            Optional<HolderLookup.RegistryLookup<Enchantment>> lookup =
                    level.registryAccess().lookup(Registries.ENCHANTMENT);
            if (lookup.isPresent()) {
                return lookup.get().get(key)
                        .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                        .orElse(0);
            }
        }
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.unwrapKey().map(key::equals).orElse(false)) {
                return enchantments.getLevel(holder);
            }
        }
        return 0;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        int used = this.getUseDuration(stack, entity) - timeLeft;
        float charge = Math.min(1.0F, (float) used / (float) fullChargeTicks(stack, level));
        float power = (charge * charge + 2.0F * charge) / 3.0F;
        if (power < 0.1F) {
            return;
        }
        ItemStack ammo = findAmmo(player);
        if (ammo.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float spread = EnchantmentHelper.processProjectileSpread(serverLevel, stack, player, 0.0F);
        int arrows = spread > 0.0F ? 3 : 1;
        float step = arrows == 1 ? 0.0F : 2.0F * spread / (float) (arrows - 1);
        float start = (float) ((arrows - 1) % 2) * step / 2.0F;
        float sign = 1.0F;
        double modBonus = Math.max(0.0D, attackDamage() - 1.0D) * 0.5D;
        for (int i = 0; i < arrows; i++) {
            float offset = start + sign * (float) ((i + 1) / 2) * step;
            sign = -sign;
            ItemStack projectile = ammo.copyWithCount(1);
            AbstractArrow arrow = projectile.getItem() instanceof ArrowItem arrowItem
                    ? arrowItem.createArrow(serverLevel, projectile, player, stack)
                    : new net.minecraft.world.entity.projectile.Arrow(serverLevel, player, projectile, stack);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + offset, 0.0F, power * 3.0F, 1.0F);
            if (charge >= 1.0F) {
                arrow.setCritArrow(true);
            }
            if (modBonus > 0.0D) {
                arrow.setBaseDamage(arrow.getBaseDamage() + modBonus);
            }
            serverLevel.addFreshEntity(arrow);
        }
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F, 1.0F / (serverLevel.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        if (!player.getAbilities().instabuild) {
            ammo.shrink(1);
        }
    }

    /** Looks for any arrow in the inventory (the weapon is not a vanilla bow). */
    private static ItemStack findAmmo(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.getItem() instanceof ArrowItem) {
                return candidate;
            }
        }
        return player.getAbilities().instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return true;
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 30;
    }
}
