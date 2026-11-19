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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import net.minecraft.world.level.Level;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.compat.ApotheosisCompat;
import xiabis.modpack.toolbox.compat.StaffCompat;
import xiabis.modpack.toolbox.compat.WrenchCompat;
import xiabis.modpack.toolbox.registry.ModComponents;
import xiabis.modpack.toolbox.util.ItemDelegates;
import xiabis.modpack.toolbox.compat.IronsCompat;
import xiabis.modpack.toolbox.util.ToolComponents;
import net.minecraft.world.item.Items;

public class OmniToolItem extends Item {
    public static final int DURABILITY = 79_900;
    public static final double ATTACK_DAMAGE = 4.18D;
    public static final double ATTACK_SPEED = 418.0D;
    /** Reach granted while the tool is held in the main hand (4.5 base + 5). */
    public static final double BLOCK_RANGE_BONUS = 5.0D;
    /** Iron's Spellbooks bonuses, shown by the vanilla attribute tooltip while held. */
    private static final double SPELL_POWER_BONUS = 20.0D;
    private static final double MAX_MANA_BONUS = 5_000.0D;
    public static final int DEFAULT_FORTUNE = 15;

    private static final ResourceLocation DAMAGE_ID = XmtMod.id("omni_tool_attack_damage");
    private static final ResourceLocation SPEED_ID = XmtMod.id("omni_tool_attack_speed");
    private static final ResourceLocation BLOCK_RANGE_ID = XmtMod.id("omni_tool_block_range");
    private static final ResourceLocation SPELL_POWER_ID = XmtMod.id("omni_tool_spell_power");
    private static final ResourceLocation MAX_MANA_ID = XmtMod.id("omni_tool_max_mana");
    private static final ResourceLocation COOLDOWN_ID = XmtMod.id("omni_tool_cooldown_reduction");
    private static final ResourceLocation MANA_REGEN_ID = XmtMod.id("omni_tool_mana_regen");

    public OmniToolItem(Properties properties) {
        super(properties);
    }

    public static ToolMode mode(ItemStack stack) {
        return ToolMode.byOrdinal(stack.getOrDefault(ModComponents.TOOL_MODE, 0));
    }

    public static boolean silkMode(ItemStack stack) {
        return stack.getOrDefault(ModComponents.SILK_MODE, false);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(DAMAGE_ID, ATTACK_DAMAGE - 1.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(SPEED_ID, ATTACK_SPEED - 4.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.BLOCK_INTERACTION_RANGE,
                        new AttributeModifier(BLOCK_RANGE_ID, BLOCK_RANGE_BONUS,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
        addIronsAttributes(builder);
        return builder.build();
    }

    /**
     * Adds the Iron's Spellbooks bonuses as regular item attribute modifiers, so the vanilla
     * "When in main hand / either hand" tooltip renders them without any custom text. They are
     * only present while the item is actually equipped in a hand.
     */
    private static void addIronsAttributes(ItemAttributeModifiers.Builder builder) {
        if (!IronsCompat.available()) {
            return;
        }
        Holder<Attribute> spellPower = IronsCompat.spellPower();
        if (spellPower != null) {
            builder.add(spellPower,
                    new AttributeModifier(SPELL_POWER_ID, SPELL_POWER_BONUS,
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.HAND);
        }
        Holder<Attribute> maxMana = IronsCompat.maxMana();
        if (maxMana != null) {
            // Max mana is a flat attribute (base 100), so a plain +5000 is what we want.
            builder.add(maxMana,
                    new AttributeModifier(MAX_MANA_ID, MAX_MANA_BONUS, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.HAND);
        }
        Holder<Attribute> cooldownReduction = IronsCompat.cooldownReduction();
        if (cooldownReduction != null) {
            builder.add(cooldownReduction,
                    new AttributeModifier(COOLDOWN_ID, 5.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    EquipmentSlotGroup.HAND);
        }
        Holder<Attribute> manaRegen = IronsCompat.manaRegen();
        if (manaRegen != null) {
            builder.add(manaRegen,
                    new AttributeModifier(MANA_REGEN_ID, 5.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    EquipmentSlotGroup.HAND);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            applyState(stack, level.registryAccess());
        }
    }

    /** Rebuilds the tool component / model state when the stored mode changed. */
    public static void applyState(ItemStack stack, RegistryAccess access) {
        ToolMode mode = mode(stack);
        boolean silk = silkMode(stack);
        int wanted = mode.customModelData(silk);
        CustomModelData current = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        boolean upToDate = current != null
                && current.value() == wanted
                && stack.has(DataComponents.TOOL)
                && stack.getOrDefault(ModComponents.STATE_APPLIED, false);
        if (upToDate) {
            return;
        }
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(wanted));
        stack.set(DataComponents.TOOL, ToolComponents.build(mode));
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        applyEnchantments(stack, access, silk);
        ApotheosisCompat.applyDefaults(stack);
        stack.set(ModComponents.STATE_APPLIED, true);
    }

    private static void applyEnchantments(ItemStack stack, RegistryAccess access, boolean silk) {
        if (access == null) {
            return;
        }
        Optional<HolderLookup.RegistryLookup<Enchantment>> lookup = access.lookup(Registries.ENCHANTMENT);
        if (lookup.isEmpty()) {
            return;
        }
        HolderLookup.RegistryLookup<Enchantment> enchantments = lookup.get();
        Optional<Holder.Reference<Enchantment>> fortune = enchantments.get(Enchantments.FORTUNE);
        Optional<Holder.Reference<Enchantment>> silkTouch = enchantments.get(Enchantments.SILK_TOUCH);
        if (!stack.has(DataComponents.ENCHANTMENTS)) {
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            fortune.ifPresent(holder -> mutable.set(holder, silk ? 0 : DEFAULT_FORTUNE));
            silkTouch.ifPresent(holder -> mutable.set(holder, silk ? 1 : 0));
        });
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        ToolMode mode = mode(stack);
        if (ability == ItemAbilities.PICKAXE_DIG) {
            return mode.has(ToolMode.PICKAXE);
        }
        if (ability == ItemAbilities.SHOVEL_DIG || ability == ItemAbilities.SHOVEL_FLATTEN) {
            return mode.has(ToolMode.SHOVEL);
        }
        if (ability == ItemAbilities.AXE_DIG || ability == ItemAbilities.AXE_STRIP
                || ability == ItemAbilities.AXE_SCRAPE || ability == ItemAbilities.AXE_WAX_OFF) {
            return mode.has(ToolMode.AXE);
        }
        if (ability == ItemAbilities.HOE_DIG || ability == ItemAbilities.HOE_TILL) {
            return mode.has(ToolMode.HOE);
        }
        if (ability == ItemAbilities.SHEARS_DIG || ability == ItemAbilities.SHEARS_HARVEST
                || ability == ItemAbilities.SHEARS_CARVE || ability == ItemAbilities.SHEARS_DISARM
                || ability == ItemAbilities.SHEARS_TRIM) {
            return mode.has(ToolMode.SHEARS);
        }
        if (ability == ItemAbilities.SWORD_DIG) {
            return mode.has(ToolMode.SHEARS) || mode.has(ToolMode.AXE);
        }
        if (ability == ItemAbilities.SHIELD_BLOCK) {
            return mode.has(ToolMode.SHIELD);
        }
        return super.canPerformAction(stack, ability);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ToolMode mode = mode(stack);
        // Staff first: casting a selected spell must win over blocking, otherwise "all" mode could
        // never cast. If the cast does nothing (no spell selected, on cooldown, ...) fall through to
        // the shield so the tool still blocks while holding the button.
        if (mode.has(ToolMode.STAFF) && StaffCompat.cast(level, player, hand).consumesAction()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (mode.has(ToolMode.SHIELD) && !player.isSecondaryUseActive()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        ToolMode mode = mode(stack);
        InteractionResult result = InteractionResult.PASS;
        if (player.isSecondaryUseActive() && mode.has(ToolMode.HOE)) {
            result = ItemDelegates.delegateOn(context, Items.IRON_HOE);
        } else if (mode.has(ToolMode.SHOVEL)) {
            result = ItemDelegates.delegateOn(context, Items.IRON_SHOVEL);
        }
        if (!result.consumesAction() && mode.has(ToolMode.HOE)) {
            result = ItemDelegates.delegateOn(context, Items.IRON_HOE);
        }
        if (!result.consumesAction() && mode.has(ToolMode.AXE)) {
            result = ItemDelegates.delegateOn(context, Items.IRON_AXE);
        }
        if (!result.consumesAction() && mode.has(ToolMode.SHEARS)) {
            result = ItemDelegates.delegateOn(context, Items.SHEARS);
        }
        if (!result.consumesAction() && mode.has(ToolMode.WRENCH)) {
            result = WrenchCompat.wrench(context);
        }
        if (!result.consumesAction() && mode.has(ToolMode.STAFF)) {
            result = StaffCompat.cast(context.getLevel(), player, context.getHand());
        }
        return result;
    }

    /**
     * Fallback so the tool always mines at least at the configured speed, even if the {@code tool}
     * component has not been (re)applied yet - e.g. a stack that came from an older version or one
     * that was just spawned by a command.
     */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float speed = super.getDestroySpeed(stack, state);
        if (speed > 1.0F) {
            return speed;
        }
        return state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL) ? 1.0F : ToolComponents.MINING_SPEED;
    }

    /** Netherite harvesting tier: everything that needs a tool drops for this item. */
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return mode(stack).has(ToolMode.SHIELD) ? UseAnim.BLOCK : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72_000;
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
