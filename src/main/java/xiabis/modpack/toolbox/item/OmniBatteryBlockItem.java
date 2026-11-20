package xiabis.modpack.toolbox.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import xiabis.modpack.toolbox.registry.ModComponents;

/**
 * Block item of the giant battery box. A broken battery keeps its charge, so the tooltip shows how much energy
 * the item currently carries.
 */
public class OmniBatteryBlockItem extends BlockItem {
    public OmniBatteryBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        long stored = stack.getOrDefault(ModComponents.STORED_ENERGY, 0L);
        if (stored > 0) {
            tooltip.add(Component.translatable("tooltip.xiabis_modpack_toolbox.omni_battery.energy", stored)
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
