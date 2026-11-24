package xiabis.modpack.toolbox.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import xiabis.modpack.toolbox.blockentity.OmniFluidTankBlockEntity;

public final class OmniFluidTankBlockItem extends BlockItem {
    public OmniFluidTankBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null || !data.contains("Fluids")) return;

        ListTag entries = data.copyTag().getList("Fluids", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            Fluid fluid;
            try {
                fluid = BuiltInRegistries.FLUID.get(
                        net.minecraft.resources.ResourceLocation.parse(entry.getString("Id")));
            } catch (RuntimeException ignored) {
                continue;
            }
            if (fluid == net.minecraft.world.level.material.Fluids.EMPTY) continue;
            int amount = Math.min(OmniFluidTankBlockEntity.CAPACITY, Math.max(0, entry.getInt("Amount")));
            if (amount <= 0) continue;
            Component name = new FluidStack(fluid, 1).getDisplayName();
            String quantity = amount >= OmniFluidTankBlockEntity.CAPACITY
                    ? "无限"
                    : amount + "mb/" + OmniFluidTankBlockEntity.CAPACITY + "mb";
            tooltip.add(Component.empty().append(name).append(" ").append(quantity)
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
