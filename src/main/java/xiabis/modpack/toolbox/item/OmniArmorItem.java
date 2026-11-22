package xiabis.modpack.toolbox.item;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xiabis.modpack.toolbox.util.ArmorSupport;
import xiabis.modpack.toolbox.util.KeyHints;

/**
 * The omni armor: a helmet that can also be worn in the curios head slot and grants the pack's
 * "skip the annoying parts" perks while worn.
 */
public class OmniArmorItem extends ArmorItem implements ICurioItem {
    public static final int DURABILITY = 640;

    public OmniArmorItem(Properties properties) {
        super(xiabis.modpack.toolbox.registry.ModArmorMaterials.OMNI_ARMOR, ArmorItem.Type.HELMET, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        ArmorSupport.ensureDefaults(stack, level);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        ArmorSupport.ensureDefaults(stack, slotContext.entity().level());
    }

    /** The pack is about convenience, so every enchantment is allowed. */
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return true;
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return true;
    }

    /**
     * The armor and toughness values are plain item attributes, so the vanilla tooltip lists them
     * ("When on Head: +60 Armor / +60 Armor Toughness") instead of custom text.
     */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(ArmorSupport.ARMOR_ID,
                                ArmorSupport.ARMOR_BONUS,
                                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(ArmorSupport.TOUGHNESS_ID,
                                ArmorSupport.TOUGHNESS_BONUS,
                                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.xiabis_modpack_toolbox.open_config", KeyHints.armorMenu()));
    }
}
