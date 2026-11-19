package xiabis.modpack.toolbox.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * Reflection based access to the attributes Iron's Spellbooks registers. The dependency is
 * optional, so the fields are only resolved when the mod is present and every accessor is
 * nullable.
 */
public final class IronsCompat {
    private static final String ATTRIBUTE_REGISTRY =
            "io.redspace.ironsspellbooks.api.registry.AttributeRegistry";

    private static boolean resolved;
    @Nullable
    private static Holder<Attribute> spellPower;
    @Nullable
    private static Holder<Attribute> maxMana;
    @Nullable
    private static Holder<Attribute> cooldownReduction;
    @Nullable
    private static Holder<Attribute> manaRegen;

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        if (!ModList.get().isLoaded("irons_spellbooks")) {
            return;
        }
        try {
            Class<?> registry = Class.forName(ATTRIBUTE_REGISTRY);
            spellPower = holder(registry, "SPELL_POWER");
            maxMana = holder(registry, "MAX_MANA");
            cooldownReduction = holder(registry, "COOLDOWN_REDUCTION");
            manaRegen = holder(registry, "MANA_REGEN");
        } catch (ReflectiveOperationException | LinkageError exception) {
            spellPower = null;
            maxMana = null;
            cooldownReduction = null;
            manaRegen = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static Holder<Attribute> holder(Class<?> registry, String name)
            throws ReflectiveOperationException {
        Field field = registry.getField(name);
        if (!Modifier.isStatic(field.getModifiers())) {
            return null;
        }
        Object value = field.get(null);
        return value instanceof Holder<?> holder ? (Holder<Attribute>) holder : null;
    }

    /** True when every attribute this mod wants to touch exists. */
    public static boolean available() {
        resolve();
        return spellPower != null && maxMana != null && cooldownReduction != null && manaRegen != null;
    }

    @Nullable
    public static Holder<Attribute> spellPower() {
        resolve();
        return spellPower;
    }

    @Nullable
    public static Holder<Attribute> maxMana() {
        resolve();
        return maxMana;
    }

    @Nullable
    public static Holder<Attribute> cooldownReduction() {
        resolve();
        return cooldownReduction;
    }

    @Nullable
    public static Holder<Attribute> manaRegen() {
        resolve();
        return manaRegen;
    }

    private IronsCompat() {
    }
}
