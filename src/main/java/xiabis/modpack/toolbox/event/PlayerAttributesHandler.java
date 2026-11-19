package xiabis.modpack.toolbox.event;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.compat.IronsCompat;
import xiabis.modpack.toolbox.item.OmniToolItem;
import xiabis.modpack.toolbox.item.OmniWeaponItem;

/**
 * Hands out the "quality of life" attribute bonuses while the toolbox items are held.
 *
 * <p>All values are applied as transient modifiers from the server: the touched attributes are
 * syncable, so the client receives the boosted values (which the client side interaction range
 * ray casts read) without any custom packets.</p>
 */
@EventBusSubscriber(modid = XmtMod.MOD_ID)
public final class PlayerAttributesHandler {
    public static final double TOOL_BLOCK_RANGE_BONUS = 5.0D;
    public static final double WEAPON_ENTITY_RANGE_BONUS = 4.0D;
    public static final double SPELL_POWER_BONUS = 20.0D;
    public static final double MAX_MANA_BONUS = 5_000.0D;
    public static final double COOLDOWN_SPEED_BONUS = 5.0D;
    public static final double MANA_REGEN_BONUS = 5.0D;

    private static final ResourceLocation TOOL_BLOCK_RANGE_ID = XmtMod.id("omni_tool_block_range");
    private static final ResourceLocation WEAPON_ENTITY_RANGE_ID = XmtMod.id("omni_weapon_entity_range");
    private static final ResourceLocation TOOL_SPELL_POWER_ID = XmtMod.id("omni_tool_spell_power");
    private static final ResourceLocation TOOL_MAX_MANA_ID = XmtMod.id("omni_tool_max_mana");
    private static final ResourceLocation TOOL_COOLDOWN_ID = XmtMod.id("omni_tool_cooldown_reduction");
    private static final ResourceLocation TOOL_MANA_REGEN_ID = XmtMod.id("omni_tool_mana_regen");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            apply(player);
        }
    }

    /** Re-evaluates every bonus for the given player. Safe to call every tick. */
    public static void apply(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean toolInMainHand = mainHand.getItem() instanceof OmniToolItem;
        boolean toolInOffHand = offHand.getItem() instanceof OmniToolItem;
        boolean weaponInMainHand = mainHand.getItem() instanceof OmniWeaponItem;
        boolean magic = toolInMainHand || toolInOffHand;

        set(player, Attributes.BLOCK_INTERACTION_RANGE, TOOL_BLOCK_RANGE_ID,
                toolInMainHand ? TOOL_BLOCK_RANGE_BONUS : 0.0D);
        set(player, Attributes.ENTITY_INTERACTION_RANGE, WEAPON_ENTITY_RANGE_ID,
                weaponInMainHand ? WEAPON_ENTITY_RANGE_BONUS : 0.0D);

        set(player, IronsCompat.spellPower(), TOOL_SPELL_POWER_ID, magic ? SPELL_POWER_BONUS : 0.0D);
        set(player, IronsCompat.maxMana(), TOOL_MAX_MANA_ID, magic ? MAX_MANA_BONUS : 0.0D);
        set(player, IronsCompat.cooldownReduction(), TOOL_COOLDOWN_ID, magic ? COOLDOWN_SPEED_BONUS : 0.0D);
        set(player, IronsCompat.manaRegen(), TOOL_MANA_REGEN_ID, magic ? MANA_REGEN_BONUS : 0.0D);
    }

    private static void set(Player player, @Nullable Holder<Attribute> attribute, ResourceLocation id,
            double amount) {
        if (attribute == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (amount <= 0.0D) {
            if (instance.getModifier(id) != null) {
                instance.removeModifier(id);
            }
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (existing == null || existing.amount() != amount) {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private PlayerAttributesHandler() {
    }
}
