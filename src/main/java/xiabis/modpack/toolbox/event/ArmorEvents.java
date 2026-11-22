package xiabis.modpack.toolbox.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.util.ArmorSupport;

/**
 * Server side effects of the omni armor: the armor / toughness bonus, the immunity to every damage
 * source except {@code /kill} and the removal of darkness / blindness.
 */
@EventBusSubscriber(modid = XmtMod.MOD_ID)
public final class ArmorEvents {
    private ArmorEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean worn = ArmorSupport.worn(player);
        ArmorSupport.applyAttributes(player, worn);
        if (worn) {
            ArmorSupport.clearBlindingEffects(player);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!ArmorSupport.worn(player)) {
            return;
        }
        // "/kill" and only "/kill": everything else is blocked by design.
        if (event.getSource().is(DamageTypes.GENERIC_KILL)) {
            return;
        }
        event.setCanceled(true);
    }
}
