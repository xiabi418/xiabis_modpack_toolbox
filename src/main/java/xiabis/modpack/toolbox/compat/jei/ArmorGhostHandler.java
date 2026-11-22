package xiabis.modpack.toolbox.compat.jei;

import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import xiabis.modpack.toolbox.client.OmniArmorScreen;
import xiabis.modpack.toolbox.menu.OmniArmorMenu;
import xiabis.modpack.toolbox.network.SetArmorMarkerPayload;

/**
 * Lets the player drag a block from JEI straight into one of the armor marker slots.
 */
public class ArmorGhostHandler implements IGhostIngredientHandler<OmniArmorScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(OmniArmorScreen screen, ITypedIngredient<I> ingredient,
            boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();
        if (!(ingredient.getIngredient() instanceof ItemStack stack) || stack.isEmpty()) {
            return targets;
        }
        if (!(stack.getItem() instanceof BlockItem)) {
            return targets;
        }
        for (int index = 0; index < OmniArmorMenu.MARKER_SLOTS; index++) {
            Slot slot = screen.getMenu().slots.get(index);
            int x = screen.getGuiLeft() + slot.x;
            int y = screen.getGuiTop() + slot.y;
            int marker = index;
            targets.add(new Target<I>() {
                @Override
                public Rect2i getArea() {
                    return new Rect2i(x, y, 16, 16);
                }

                @Override
                public void accept(I ignored) {
                    PacketDistributor.sendToServer(new SetArmorMarkerPayload(marker, stack.copyWithCount(1)));
                }
            });
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }
}
