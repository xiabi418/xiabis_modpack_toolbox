package xiabis.modpack.toolbox.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import xiabis.modpack.toolbox.blockentity.OmniDiggerBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlocks;
import xiabis.modpack.toolbox.registry.ModMenus;

public final class OmniDiggerMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final OmniDiggerBlockEntity digger;
    private final ContainerData data;

    public OmniDiggerMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), null);
    }

    public OmniDiggerMenu(int id, Inventory inventory, BlockPos pos, OmniDiggerBlockEntity digger) {
        super(ModMenus.OMNI_DIGGER.get(), id);
        this.digger = digger;
        access = digger == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(inventory.player.level(), pos);
        data = digger == null ? new SimpleContainerData(8) : new ContainerData() {
            public int get(int index) { return digger.menuValue(index); }
            public void set(int index, int value) { }
            public int getCount() { return 8; }
        };
        addDataSlots(data);
    }

    @Override public boolean stillValid(Player player) {
        return (digger == null || (!digger.isRemoved() && digger.getLevel() == player.level()))
                && stillValid(access, player, ModBlocks.OMNI_DIGGER.get());
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (digger == null || !stillValid(player) || !player.getAbilities().mayBuild) return false;
        switch (id) {
            case 0 -> digger.toggleMode();
            case 1 -> digger.toggleEnabled();
            default -> { return false; }
        }
        return true;
    }

    public int energy() { return (data.get(0) & 0xffff) | (data.get(1) << 16); }
    public int status() { return data.get(2); }
    public int currentY() { return data.get(3); }
    public int bottomY() { return data.get(4); }
    public int progress() { return data.get(5); }
    public boolean silk() { return data.get(6) != 0; }
    public boolean enabled() { return data.get(7) != 0; }
    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
}
