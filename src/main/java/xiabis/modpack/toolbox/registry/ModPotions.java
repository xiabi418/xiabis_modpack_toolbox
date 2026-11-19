package xiabis.modpack.toolbox.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.compat.ApothicCompat;

/**
 * The 24 hour flight potion reuses the "flying" effect shipped by Apothic Attributes (a hard
 * dependency of Apotheosis) instead of registering a second flight effect. When Apothic Attributes
 * is missing the potion is registered without effects and the recipe stays disabled by its
 * {@code neoforge:mod_loaded} condition.
 */
public final class ModPotions {
    /** 24 in-game hours (86400 seconds). */
    public static final int FLIGHT_DURATION_TICKS = 86_400 * 20;
    /** Suffix used by {@code PotionItem}: the lang key becomes item.minecraft.potion.effect.&lt;name&gt;. */
    public static final String FLIGHT_NAME = "xiabis_flight";

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, XmtMod.MOD_ID);

    public static final DeferredHolder<Potion, Potion> FLIGHT =
            POTIONS.register("flight", () -> ApothicCompat.flyingEffect()
                    .map(effect -> new Potion(FLIGHT_NAME,
                            new MobEffectInstance(effect, FLIGHT_DURATION_TICKS, 0)))
                    .orElseGet(() -> new Potion(FLIGHT_NAME)));

    public static boolean hasFlightEffect() {
        return !FLIGHT.get().getEffects().isEmpty();
    }

    private ModPotions() {
    }
}
