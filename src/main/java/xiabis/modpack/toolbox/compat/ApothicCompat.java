package xiabis.modpack.toolbox.compat;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/**
 * Apothic Attributes (a hard dependency of Apotheosis) already ships a "flying" effect that grants
 * the creative flight attribute, so this mod reuses it instead of registering a duplicate effect.
 */
public final class ApothicCompat {
    public static final ResourceLocation FLYING_EFFECT =
            ResourceLocation.fromNamespaceAndPath("apothic_attributes", "flying");
    public static final int FLIGHT_POTION_COLOR = 0x9AD9FF;

    public static Optional<Holder<MobEffect>> flyingEffect() {
        Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.getHolder(FLYING_EFFECT);
        return holder.isPresent() ? Optional.of(holder.get()) : Optional.empty();
    }


    private ApothicCompat() {
    }
}
