package xiabis.modpack.toolbox.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

/**
 * The potion flask stores the effects the player marked while sneaking. Every entry keeps the
 * strongest amplifier that has been marked so far; re-marking the same effect at the same or a
 * lower level does not change anything.
 */
public record MarkedEffects(List<MarkedEffect> entries) {
    public static final int MAX_ENTRIES = 16;
    public static final MarkedEffects EMPTY = new MarkedEffects(List.of());

    public static final Codec<MarkedEffects> CODEC = MarkedEffect.CODEC.listOf()
            .xmap(MarkedEffects::new, MarkedEffects::entries);

    public MarkedEffects {
        entries = List.copyOf(entries);
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public Optional<MarkedEffect> find(Holder<MobEffect> effect) {
        return this.entries.stream().filter(entry -> entry.effect().equals(effect)).findFirst();
    }

    /**
     * Adds the given effect, or upgrades an already marked entry when {@code level} is higher.
     * Returns {@code this} when nothing changed.
     */
    public MarkedEffects mark(Holder<MobEffect> effect, int level) {
        if (level <= 0) {
            return this;
        }
        List<MarkedEffect> copy = new ArrayList<>(this.entries);
        Optional<MarkedEffect> existing = this.find(effect);
        if (existing.isPresent()) {
            MarkedEffect entry = existing.get();
            if (entry.level() >= level) {
                return this;
            }
            copy.set(copy.indexOf(entry), new MarkedEffect(effect, level));
            return new MarkedEffects(copy);
        }
        if (copy.size() >= MAX_ENTRIES) {
            return this;
        }
        copy.add(new MarkedEffect(effect, level));
        return new MarkedEffects(copy);
    }

    /** Marked effects sorted by their translation key so the tooltip order is stable. */
    public List<MarkedEffect> sorted() {
        return this.entries.stream()
                .sorted(Comparator.comparing(entry -> entry.effect().value().getDescriptionId()))
                .toList();
    }

    public record MarkedEffect(Holder<MobEffect> effect, int level) {
        public static final Codec<MarkedEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MobEffect.CODEC.fieldOf("effect").forGetter(MarkedEffect::effect),
                Codec.intRange(1, 256).fieldOf("level").forGetter(MarkedEffect::level)
        ).apply(instance, MarkedEffect::new));
    }
}
