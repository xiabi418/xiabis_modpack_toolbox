package xiabis.modpack.toolbox.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import xiabis.modpack.toolbox.registry.ModItems;
import xiabis.modpack.toolbox.registry.ModMenus;
import xiabis.modpack.toolbox.util.ArmorMarkers;
import xiabis.modpack.toolbox.util.ArmorSlotTarget;
import xiabis.modpack.toolbox.util.ArmorSupport;

/**
 * The 4x9 marker editor of the omni armor.
 */
public class OmniArmorMenu extends AbstractContainerMenu {
    public static final int COLUMNS = 9;
    public static final int ROWS = 4;
    public static final int MARKER_SLOTS = COLUMNS * ROWS;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 20;
    private static final int PLAYER_X = 8;
    private static final int PLAYER_Y = 104;
    private static final int HOTBAR_Y = 162;

    private final Container markers;
    private final Player player;

    public OmniArmorMenu(int id, Inventory inventory, ArmorSlotTarget target) {
        super(ModMenus.OMNI_ARMOR.get(), id);
        this.player = inventory.player;
        if (inventory.player.level().isClientSide) {
            this.markers = new SimpleContainer(MARKER_SLOTS);
        } else {
            ItemStack armor = ArmorSupport.resolve(inventory.player, target);
            this.markers = new ArmorMarkerContainer(inventory.player, target, ArmorMarkers.get(armor));
        }
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;
                addSlot(new GhostSlot(markers, index, GRID_X + col * 18, GRID_Y + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                addSlot(new Slot(inventory, col + row * COLUMNS + 9, PLAYER_X + col * 18,
                        PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < COLUMNS; col++) {
            addSlot(new Slot(inventory, col, PLAYER_X + col * 18, HOTBAR_Y));
        }
    }

    /** Used by the JEI ghost handler to drop one item into a marker slot. */
    public void setMarker(int slot, ItemStack stack) {
        if (slot < 0 || slot >= MARKER_SLOTS) {
            return;
        }
        markers.setItem(slot, stack);
        broadcastChanges();
    }

    /**
     * The marker slots are ghosts: a click marks the block without moving any item, and the stacks
     * inside them can never be taken out (which used to duplicate items through JEI drags).
     */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < MARKER_SLOTS) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                ItemStack carried = getCarried();
                if (carried.isEmpty()) {
                    markers.setItem(slotId, ItemStack.EMPTY);
                } else if (carried.getItem() instanceof BlockItem) {
                    markers.setItem(slotId, carried.copyWithCount(1));
                }
                broadcastChanges();
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || index < MARKER_SLOTS) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (!moveItemStackTo(stack, MARKER_SLOTS, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.player && ArmorSupport.hasArmor(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        markers.setChanged();
    }

    /** Read only slot used for the marker grid. */
    private static final class GhostSlot extends Slot {
        private GhostSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
