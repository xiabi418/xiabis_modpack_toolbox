package xiabis.modpack.toolbox.event;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.item.OmniToolItem;
import xiabis.modpack.toolbox.item.OmniWeaponItem;
import xiabis.modpack.toolbox.item.ToolMode;
import xiabis.modpack.toolbox.world.AccelerationDisplay;
import xiabis.modpack.toolbox.world.AccelerationManager;
import xiabis.modpack.toolbox.util.ToolComponents;

@EventBusSubscriber(modid = XmtMod.MOD_ID)
public final class GameEvents {
    /**
     * Omnipotent items accept every enchantment, including mutually exclusive ones.
     * The anvil merge is performed here so conflicts can be bypassed.
     */
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        if (!isOmni(left) || event.getRight().isEmpty()) {
            return;
        }
        ItemStack right = event.getRight();
        Map<Holder<Enchantment>, Integer> merged = new LinkedHashMap<>();
        for (Map.Entry<Holder<Enchantment>, Integer> entry
                : EnchantmentHelper.getEnchantmentsForCrafting(left).entrySet()) {
            merged.put(entry.getKey(), entry.getValue());
        }

        boolean changed = false;
        if (right.is(Items.ENCHANTED_BOOK) || isOmni(right)) {
            for (Map.Entry<Holder<Enchantment>, Integer> entry
                    : EnchantmentHelper.getEnchantmentsForCrafting(right).entrySet()) {
                int level = entry.getValue();
                Integer existing = merged.get(entry.getKey());
                int newLevel = existing == null ? level : Math.max(existing, level);
                if (existing != null && existing.equals(newLevel)) {
                    newLevel = Math.min(255, existing + 1);
                }
                if (existing == null || newLevel != existing) {
                    merged.put(entry.getKey(), newLevel);
                    changed = true;
                }
            }
        }
        if (!changed) {
            return;
        }
        ItemStack output = left.copy();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        merged.forEach(mutable::set);
        EnchantmentHelper.setEnchantments(output, mutable.toImmutable());
        event.setOutput(output);
        event.setCost(Math.max(1L, merged.values().stream().mapToLong(Integer::longValue).sum() / 2L));
        event.setMaterialCost(0);
    }

    /**
     * Sonic booms are tagged as bypassing shields, so a vanilla shield (and therefore the shield
     * mode of the omni tool) cannot stop a warden's sonic boom. Handle it here instead: while the
     * tool is held up in shield mode the damage is swallowed and the normal block sound plays.
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!event.getSource().is(DamageTypes.SONIC_BOOM) || !player.isBlocking()) {
            return;
        }
        ItemStack useItem = player.getUseItem();
        if (!(useItem.getItem() instanceof OmniToolItem) || !OmniToolItem.mode(useItem).has(ToolMode.SHIELD)) {
            return;
        }
        event.setCanceled(true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Both omni items dig every block at the same time: 18 ticks (0.9s).
     * Vanilla progress per tick is {@code digSpeed / hardness / 30}, so the required
     * speed is proportional to the block hardness; this replaces the vanilla value
     * (which already contains haste/water/airborne modifiers) with that exact figure.
     */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!isOmni(player.getMainHandItem())) {
            return;
        }
        BlockState state = event.getState();
        BlockPos pos = event.getPosition().orElse(BlockPos.ZERO);
        float hardness = state.getDestroySpeed(player.level(), pos);
        if (hardness <= 0.0F) {
            // Unbreakable (-1) or instantly broken (0) blocks keep their normal handling.
            return;
        }
        event.setNewSpeed(hardness * ToolComponents.SPEED_PER_HARDNESS);
    }

    /**
     * Drives the time wand: extra game ticks for every accelerated block, plus the periodic
     * particle and overlay refresh.
     */
    @SubscribeEvent
    public static void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        AccelerationDisplay.clearAll(event.getServer());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        AccelerationManager.tick(level);
        if (level.getGameTime() % 10L == 0L) {
            AccelerationManager.spawnParticles(level);
        }
    }

    private static boolean isOmni(ItemStack stack) {
        return stack.getItem() instanceof OmniToolItem || stack.getItem() instanceof OmniWeaponItem;
    }

    private GameEvents() {
    }
}
