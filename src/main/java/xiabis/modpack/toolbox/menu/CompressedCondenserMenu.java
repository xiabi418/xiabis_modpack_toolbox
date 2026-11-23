package xiabis.modpack.toolbox.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import xiabis.modpack.toolbox.blockentity.CompressedCondenserBlockEntity;
import xiabis.modpack.toolbox.registry.ModMenus;

/** Ghost filter markers and real AE2 card/cell slots. */
public final class CompressedCondenserMenu extends AEBaseMenu {
    private static final int FILTER_COUNT = 27;
    private static final int CARD_COUNT = 6;
    private static final int DISK_SLOT = FILTER_COUNT + CARD_COUNT;
    private static final int PLAYER_START = DISK_SLOT + 1;

    private final CompressedCondenserBlockEntity condenser;
    @GuiSync(0) private long storedValue;
    @GuiSync(1) private boolean singularityMode;
    @GuiSync(2) private boolean activeOutput;
    @GuiSync(3) private long rate;

    public CompressedCondenserMenu(int id, Inventory inventory, CompressedCondenserBlockEntity be) {
        super(ModMenus.COMPRESSED_CONDENSER.get(), id, inventory, be);
        condenser = be;
        Container filters = be == null || isClientSide() ? new SimpleContainer(FILTER_COUNT) : new FilterContainer(be);
        for (int i = 0; i < FILTER_COUNT; i++) addSlot(new GhostSlot(filters, i), SlotSemantics.CONFIG);
        var cards = be != null ? be.cards() : new ItemStackHandler(CARD_COUNT);
        for (int i = 0; i < CARD_COUNT; i++) addSlot(new SlotItemHandler(cards, i, 0, 0), SlotSemantics.UPGRADE);
        IItemHandlerModifiable disk = be != null ? new DiskHandler(be) : new ItemStackHandler(1);
        addSlot(new SlotItemHandler(disk, 0, 0, 0), SlotSemantics.STORAGE_CELL);
        createPlayerInventorySlots(inventory);
    }

    public static CompressedCondenserMenu fromBuffer(int id, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        var be = inventory.player.level().getBlockEntity(pos);
        return new CompressedCondenserMenu(id, inventory,
                be instanceof CompressedCondenserBlockEntity condenser ? condenser : null);
    }

    public CompressedCondenserBlockEntity condenser() { return condenser; }
    public long displayedValue() { return storedValue; }
    public boolean singularityMode() { return singularityMode; }
    public boolean activeOutput() { return activeOutput; }
    public long rate() { return rate; }

    @Override public void broadcastChanges() {
        if (isServerSide() && condenser != null) {
            storedValue = condenser.storedValue();
            singularityMode = condenser.singularityMode();
            activeOutput = condenser.activeOutput();
            rate = condenser.rate();
        }
        super.broadcastChanges();
    }

    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < FILTER_COUNT) {
            if (clickType == ClickType.PICKUP) {
                ItemStack carried = getCarried();
                getSlot(slotId).set(carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override public boolean canDragTo(Slot slot) {
        return !(slot instanceof GhostSlot) && super.canDragTo(slot);
    }

    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof GhostSlot) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < FILTER_COUNT || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index < PLAYER_START) {
            if (!moveItemStackTo(source, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(source, DISK_SLOT, DISK_SLOT + 1, false)
                && !moveItemStackTo(source, FILTER_COUNT, DISK_SLOT, false)) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player) {
        return condenser != null && !condenser.isRemoved()
                && player.level().getBlockEntity(condenser.getBlockPos()) == condenser
                && condenser.getBlockPos().distToCenterSqr(player.position()) <= 64.0D;
    }

    private static final class GhostSlot extends Slot {
        private GhostSlot(Container container, int index) { super(container, index, 0, 0); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }

    private static final class FilterContainer extends SimpleContainer {
        private final CompressedCondenserBlockEntity be;
        private FilterContainer(CompressedCondenserBlockEntity be) { super(FILTER_COUNT); this.be = be; }
        @Override public ItemStack getItem(int slot) { return be.filters().getStackInSlot(slot); }
        @Override public void setItem(int slot, ItemStack stack) { be.filters().setStackInSlot(slot, stack); }
    }

    private static final class DiskHandler implements IItemHandlerModifiable {
        private final CompressedCondenserBlockEntity be;
        private DiskHandler(CompressedCondenserBlockEntity be) { this.be = be; }
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? be.disk() : ItemStack.EMPTY; }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (slot == 0 && (stack.isEmpty() || stack.getCount() == 1) && be.canSetDisk(stack)) be.setDisk(stack);
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !be.canSetDisk(stack) || !be.disk().isEmpty()) return stack;
            if (!simulate) be.setDisk(stack);
            return stack.getCount() == 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || be.disk().isEmpty()) return ItemStack.EMPTY;
            return simulate ? be.disk().copy() : be.takeDisk();
        }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && be.canSetDisk(stack); }
    }
}
