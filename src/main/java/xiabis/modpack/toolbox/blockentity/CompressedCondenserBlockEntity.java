package xiabis.modpack.toolbox.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.CondenserOutput;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.core.definitions.AEItems;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import xiabis.modpack.toolbox.menu.CompressedCondenserMenu;
import xiabis.modpack.toolbox.registry.ModBlockEntities;

public final class CompressedCondenserBlockEntity extends AENetworkedBlockEntity implements net.minecraft.world.MenuProvider {
    public static final int FILTER_SLOTS = 27;
    public static final int CARD_SLOTS = 6;
    public static final long BASE_RATE = 102_400L;

    private final ItemStackHandler filters = new ItemStackHandler(FILTER_SLOTS) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public int getSlotLimit(int slot) { return 1; }
    };
    private final ItemStackHandler cards = new ItemStackHandler(CARD_SLOTS) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            int type = cardType(stack);
            if (type < 0) return false;
            int count = 0;
            for (int i = 0; i < CARD_SLOTS; i++) if (cardType(getStackInSlot(i)) == type) count++;
            return count < (type == 3 ? 5 : 1);
        }
    };
    private ItemStack disk = ItemStack.EMPTY;
    private long storedValue;
    private boolean activeOutput;
    private boolean singularityMode;

    public CompressedCondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPRESSED_CONDENSER.get(), pos, state);
    }

    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(appeng.api.AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.COMPRESSED_CONDENSER.get(), (condenser, context) -> condenser);
    }

    public ItemStackHandler filters() { return filters; }
    public ItemStackHandler cards() { return cards; }
    public ItemStack disk() { return disk; }
    public boolean canSetDisk(ItemStack stack) { return stack.isEmpty() || (stack.getItem() instanceof IBasicCellItem cell
            && cell.getKeyType() == AEKeyType.items() && StorageCells.isCellHandled(stack)); }
    public void setDisk(ItemStack stack) { disk = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1); setChanged(); }
    public ItemStack takeDisk() { ItemStack result = disk; disk = ItemStack.EMPTY; setChanged(); return result; }
    public boolean activeOutput() { return activeOutput; }
    public boolean singularityMode() { return singularityMode; }
    public void toggleOutput() { activeOutput = !activeOutput; setChanged(); }
    public void toggleMode() { singularityMode = !singularityMode; setChanged(); }
    public long storedValue() { return storedValue; }
    public long cost() { return Math.max(1, (singularityMode ? CondenserOutput.SINGULARITY : CondenserOutput.MATTER_BALLS).requiredPower); }
    public long rate() { return BASE_RATE * (1L + 10L * speedCards()); }
    private int speedCards() { int count = 0; for (int i = 0; i < CARD_SLOTS; i++) if (cardType(cards.getStackInSlot(i)) == 3) count++; return Math.min(5, count); }
    private boolean hasCard(int type) { for (int i = 0; i < CARD_SLOTS; i++) if (cardType(cards.getStackInSlot(i)) == type) return true; return false; }
    private static int cardType(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !id.getNamespace().equals("ae2")) return -1;
        return switch (id.getPath()) { case "inverter_card" -> 0; case "redstone_card" -> 1; case "fuzzy_card" -> 2; case "speed_card" -> 3; default -> -1; };
    }
    public boolean hasDisk() { return !disk.isEmpty() && canSetDisk(disk); }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, CompressedCondenserBlockEntity be) {
        if (!be.getMainNode().isOnline() || !be.hasDisk()) return;
        if (be.hasCard(1) && !level.hasNeighborSignal(pos)) return;
        IGrid grid = be.getMainNode().getGrid();
        if (grid == null) return;
        MEStorage network = grid.getStorageService().getInventory();
        IActionSource source = IActionSource.ofMachine(be);
        long remaining = Math.min(be.rate(), Long.MAX_VALUE - be.storedValue);
        if (remaining <= 0) {
            be.emit(network, source);
            return;
        }
        boolean inverter = be.hasCard(0);
        boolean fuzzy = be.hasCard(2);
        KeyCounter available = (inverter || fuzzy) ? network.getAvailableStacks() : null;
        if (available == null) {
            for (int slot = 0; slot < FILTER_SLOTS && remaining > 0; slot++) {
                ItemStack filter = be.filters.getStackInSlot(slot);
                if (filter.isEmpty()) continue;
                AEItemKey key = AEItemKey.of(filter);
                long extracted = network.extract(key, remaining, Actionable.MODULATE, source);
                be.credit(key, extracted);
                remaining -= extracted;
            }
        } else {
            for (AEKey candidate : new java.util.ArrayList<>(available.keySet())) {
                if (!(candidate instanceof AEItemKey)) continue;
                boolean matchesAny = false;
                for (int slot = 0; slot < FILTER_SLOTS; slot++) {
                    ItemStack filter = be.filters.getStackInSlot(slot);
                    if (filter.isEmpty()) continue;
                    AEItemKey filterKey = AEItemKey.of(filter);
                    if (fuzzy ? candidate.fuzzyEquals(filterKey, FuzzyMode.IGNORE_ALL) : candidate.equals(filterKey)) {
                        matchesAny = true;
                        break;
                    }
                }
                if (matchesAny == inverter) continue;
                long extracted = network.extract(candidate, remaining, Actionable.MODULATE, source);
                be.credit(candidate, extracted);
                remaining -= extracted;
                if (remaining <= 0) break;
            }
        }
        be.emit(network, source);

    }

    private void credit(AEKey key, long amount) {
        if (amount <= 0) return;
        long units = key.getAmountPerOperation();
        if (units <= 0) return;
        long value = amount / units;
        if (value > 0) {
            storedValue += value;
            setChanged();
        }
    }

    private void emit(MEStorage network, IActionSource source) {
        long cost = cost();
        long possible = storedValue / cost;
        if (possible <= 0) return;
        ItemStack output = (singularityMode ? AEItems.SINGULARITY : AEItems.MATTER_BALL).stack();
        long accepted;
        if (activeOutput) {
            accepted = network.insert(AEItemKey.of(output), possible, Actionable.MODULATE, source);
        } else {
            StorageCell cell = StorageCells.getCellInventory(disk, this::setChanged);
            if (cell == null) return;
            AEItemKey key = AEItemKey.of(output);
            long capacity = possible;
            if (cell.insert(key, capacity, Actionable.SIMULATE, source) <= 0) return;
            accepted = cell.insert(key, capacity, Actionable.MODULATE, source);
            if (accepted > 0) cell.persist();
        }
        if (accepted > 0) {
            storedValue -= accepted * cost;
            setChanged();
        }
    }

    @Override public void saveChanges() { setChanged(); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Filters", filters.serializeNBT(registries));
        tag.put("Cards", cards.serializeNBT(registries));
        if (!disk.isEmpty()) tag.put("Disk", disk.save(registries));
        tag.putLong("StoredValue", storedValue);
        tag.putBoolean("ActiveOutput", activeOutput);
        tag.putBoolean("Singularity", singularityMode);
    }
    @Override public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        filters.deserializeNBT(registries, tag.getCompound("Filters"));
        cards.deserializeNBT(registries, tag.getCompound("Cards"));
        disk = ItemStack.parseOptional(registries, tag.getCompound("Disk"));
        storedValue = Math.max(0, tag.getLong("StoredValue"));
        activeOutput = tag.getBoolean("ActiveOutput");
        singularityMode = tag.getBoolean("Singularity");
    }
    @Override public Component getDisplayName() { return Component.translatable("block.xiabis_modpack_toolbox.compressed_condenser"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new CompressedCondenserMenu(id, inventory, this); }
}
