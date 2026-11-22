package xiabis.modpack.toolbox.menu;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xiabis.modpack.toolbox.util.ArmorMarkers;
import xiabis.modpack.toolbox.util.ArmorSlotTarget;
import xiabis.modpack.toolbox.util.ArmorSupport;

/**
 * A 36 slot view over the marker list of the armor stack that is currently being edited. Every
 * mutation is written straight back onto the stack so closing the menu can never lose data.
 */
public class ArmorMarkerContainer implements Container {
    private final Player player;
    private final ArmorSlotTarget target;
    private final NonNullList<ItemStack> items;

    public ArmorMarkerContainer(Player player, ArmorSlotTarget target, List<ItemStack> initial) {
        this.player = player;
        this.target = target;
        this.items = NonNullList.withSize(ArmorMarkers.SLOTS, ItemStack.EMPTY);
        List<ItemStack> normalised = ArmorMarkers.normalize(initial);
        for (int i = 0; i < ArmorMarkers.SLOTS; i++) {
            this.items.set(i, normalised.get(i));
        }
    }

    @Override
    public int getContainerSize() {
        return ArmorMarkers.SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack current = items.get(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = current.split(amount);
        if (current.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        writeBack();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack taken = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        writeBack();
        return taken;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        writeBack();
    }

    @Override
    public void setChanged() {
        writeBack();
    }

    @Override
    public boolean stillValid(Player player) {
        return player != null && player.isAlive();
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < ArmorMarkers.SLOTS; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        writeBack();
    }

    private void writeBack() {
        ItemStack armor = ArmorSupport.resolve(player, target);
        if (!ArmorSupport.isArmor(armor)) {
            return;
        }
        ArmorMarkers.set(armor, items);
        ArmorSupport.store(player, target, armor);
        player.getInventory().setChanged();
    }
}
