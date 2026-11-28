package xiabis.modpack.toolbox.blockentity;

import com.mojang.authlib.GameProfile;
import java.util.ArrayDeque;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.event.level.BlockEvent;
import xiabis.modpack.toolbox.menu.OmniDiggerMenu;
import xiabis.modpack.toolbox.registry.ModBlockEntities;

public final class OmniDiggerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 100_000;
    private static final int FE_PER_ORE = 1_000;
    private static final int CELLS_PER_LAYER = 256;
    private final Buffer energy = new Buffer();
    // A handler may accept only part of a loot batch. Persist the rest across saves.
    private final ArrayDeque<ItemStack> pending = new ArrayDeque<>();
    private int cursor;
    private boolean enabled;
    private boolean silk;
    private int status;
    private UUID owner;
    private String ownerName = "";

    public OmniDiggerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OMNI_DIGGER.get(), pos, state);
    }

    public EnergyStorage energyStorage() { return energy; }

    public void setOwner(Player player) {
        owner = player.getUUID();
        ownerName = player.getGameProfile().getName();
        setChanged();
    }

    public void toggleMode() { silk = !silk; setChanged(); }
    public void toggleEnabled() { enabled = !enabled; setChanged(); }

    public int menuValue(int index) {
        int bottom = level == null ? 0 : level.getMinBuildHeight();
        int total = Math.max(1, cells(bottom));
        return switch (index) {
            case 0 -> energy.getEnergyStored() & 0xffff;
            case 1 -> energy.getEnergyStored() >>> 16;
            case 2 -> status;
            case 3 -> Math.max(bottom, worldPosition.getY() - cursor / CELLS_PER_LAYER);
            case 4 -> bottom;
            case 5 -> (int) Math.min(100, 100L * cursor / total);
            case 6 -> silk ? 1 : 0;
            case 7 -> enabled ? 1 : 0;
            default -> 0;
        };
    }

    private int cells(int bottom) {
        return (worldPosition.getY() - bottom + 1) * CELLS_PER_LAYER;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OmniDiggerBlockEntity digger) {
        if (level instanceof ServerLevel server && server.getGameTime() % 4 == 0) digger.work(server, pos);
    }

    private void work(ServerLevel level, BlockPos pos) {
        if (!enabled) { status = 0; return; }
        int total = cells(level.getMinBuildHeight());
        if (pending.isEmpty() && cursor >= total) { status = 4; return; }
        IItemHandler output = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), Direction.DOWN);
        if (output == null || !flush(output)) { status = 2; return; }
        if (cursor >= total) { status = 4; return; }
        if (energy.getEnergyStored() < FE_PER_ORE) { status = 3; return; }
        var actor = owner == null ? FakePlayerFactory.getMinecraft(level)
                : FakePlayerFactory.get(level, new GameProfile(owner, ownerName));
        int minX = pos.getX() & ~15;
        int minZ = pos.getZ() & ~15;
        status = 1;
        int start = cursor;
        while (cursor < total && cursor - start < CELLS_PER_LAYER) {
            BlockPos target = new BlockPos(minX + (cursor & 15), pos.getY() - cursor / CELLS_PER_LAYER,
                    minZ + ((cursor >> 4) & 15));
            BlockState ore = level.getBlockState(target);
            if (!ore.is(Tags.Blocks.ORES) || ore.getDestroySpeed(level, target) < 0
                    || !level.mayInteract(actor, target)) {
                cursor++;
                continue;
            }
            if (NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, target, ore, actor)).isCanceled()) {
                cursor++;
                continue;
            }
            ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
            var enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(silk ? Enchantments.SILK_TOUCH : Enchantments.FORTUNE);
            tool.enchant(enchantment, silk ? 1 : 15);
            var drops = Block.getDrops(ore, level, target, level.getBlockEntity(target), actor, tool);
            // Check each output type before removing the ore. Execution may still partially accept a batch.
            if (!drops.isEmpty() && drops.stream().anyMatch(stack -> !stack.isEmpty()
                    && ItemHandlerHelper.insertItemStacked(output, stack.copy(), true).getCount() == stack.getCount())) {
                status = 2;
                break;
            }
            if (level.getBlockState(target) != ore || !level.setBlock(target, ore.getFluidState().createLegacyBlock(), Block.UPDATE_ALL)) break;
            energy.spend();
            cursor++;
            drops.stream().filter(stack -> !stack.isEmpty()).forEach(pending::add);
            if (!flush(output)) status = 2;
            break;
        }
        if (cursor != start) setChanged();
    }

    private boolean flush(IItemHandler output) {
        while (!pending.isEmpty()) {
            ItemStack stack = pending.peek();
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(output, stack.copy(), false);
            if (remainder.getCount() == stack.getCount()) return false;
            pending.remove();
            if (!remainder.isEmpty()) pending.addFirst(remainder);
            setChanged();
        }
        return true;
    }

    public void dropContents(Level level, BlockPos pos) {
        for (ItemStack stack : pending) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        pending.clear();
    }

    @Override public Component getDisplayName() { return Component.translatable("block.xiabis_modpack_toolbox.omni_digger"); }
    @Override public OmniDiggerMenu createMenu(int id, Inventory inventory, Player player) {
        return new OmniDiggerMenu(id, inventory, worldPosition, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Cursor", cursor);
        tag.putBoolean("Enabled", enabled);
        tag.putBoolean("Silk", silk);
        if (owner != null) {
            tag.putUUID("Owner", owner);
            tag.putString("OwnerName", ownerName);
        }
        var drops = new ListTag();
        for (ItemStack stack : pending) drops.add(stack.save(registries));
        tag.put("Pending", drops);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.restore(tag.getInt("Energy"));
        cursor = Math.max(0, tag.getInt("Cursor"));
        enabled = tag.getBoolean("Enabled");
        silk = tag.getBoolean("Silk");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ownerName = tag.getString("OwnerName");
        pending.clear();
        for (Tag drop : tag.getList("Pending", Tag.TAG_COMPOUND)) {
            ItemStack.parse(registries, drop).filter(stack -> !stack.isEmpty()).ifPresent(pending::add);
        }
    }

    private final class Buffer extends EnergyStorage {
        Buffer() { super(CAPACITY, CAPACITY, 0); }
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int received = super.receiveEnergy(amount, simulate);
            if (!simulate && received > 0) setChanged();
            return received;
        }
        void spend() { energy -= FE_PER_ORE; }
        void restore(int stored) { energy = Math.clamp(stored, 0, CAPACITY); }
    }
}
