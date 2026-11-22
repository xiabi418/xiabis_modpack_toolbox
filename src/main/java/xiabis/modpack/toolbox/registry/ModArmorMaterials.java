package xiabis.modpack.toolbox.registry;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;

/**
 * Armor material of the omni armor. The actual armor / toughness values are applied as attribute
 * modifiers by {@code ArmorSupport}, so the vanilla defense map stays empty.
 */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, XmtMod.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> OMNI_ARMOR =
            ARMOR_MATERIALS.register("omni_armor", () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.HELMET, 0),
                    30,
                    Holder.direct(SoundEvents.ARMOR_EQUIP_NETHERITE.value()),
                    () -> Ingredient.of(Items.DIAMOND),
                    List.of(new ArmorMaterial.Layer(XmtMod.id("omni_armor"))),
                    0.0F,
                    0.0F));

    private ModArmorMaterials() {
    }
}
