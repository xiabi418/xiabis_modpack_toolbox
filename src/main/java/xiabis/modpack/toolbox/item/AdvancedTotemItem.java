package xiabis.modpack.toolbox.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** A durable totem that uses the standard death-rescue effects through the mod event hook. */
public final class AdvancedTotemItem extends Item {
    public static final int DURABILITY = 256;

    public AdvancedTotemItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.xiabis_modpack_toolbox.advanced_totem_of_undying"));
    }
}
