package xiabis.modpack.toolbox.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Thin wrapper around the Curios "head" slot so the rest of the mod does not depend on the API
 * everywhere. Curios is a required dependency, but a missing API must never crash the game.
 */
public final class CuriosSupport {
    private static final String SLOT = "head";

    private CuriosSupport() {
    }

    private static top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler headStacks(Player player) {
        if (player == null || !ModList.get().isLoaded("curios")) {
            return null;
        }
        try {
            return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                    .flatMap(handler -> handler.getStacksHandler(SLOT))
                    .map(top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler::getStacks)
                    .orElse(null);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    public static ItemStack head(Player player) {
        top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks = headStacks(player);
        return stacks == null || stacks.getSlots() == 0 ? ItemStack.EMPTY : stacks.getStackInSlot(0);
    }

    public static void setHead(Player player, ItemStack stack) {
        top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks = headStacks(player);
        if (stacks != null && stacks.getSlots() > 0) {
            stacks.setStackInSlot(0, stack);
        }
    }
}
