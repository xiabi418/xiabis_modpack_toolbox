package xiabis.modpack.toolbox.menu;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import xiabis.modpack.toolbox.blockentity.OmniGreenhouseBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlocks;
import xiabis.modpack.toolbox.registry.ModMenus;
import xiabis.modpack.toolbox.util.GreenhouseFaceMode;

/** Container of the omni greenhouse: one seed slot, nine outputs, two card slots and the GUI state. */
public class OmniGreenhouseMenu extends AbstractContainerMenu {
    /** energy, capacity, fluid, tank capacity, progress, period, activeInput, activeOutput, 6 faces. */
    public static final int DATA_COUNT = 14;

    public static final int MACHINE_SLOTS = 12;

    private static final int PLAYER_INVENTORY_X = 8;
    private static final int PLAYER_INVENTORY_Y = 104;
    private static final int HOTBAR_Y = 162;

    @Nullable
    private final OmniGreenhouseBlockEntity greenhouse;
    private final BlockPos pos;
    private final ContainerData data;

    /** Server side constructor. */
    public OmniGreenhouseMenu(int id, Inventory inventory, OmniGreenhouseBlockEntity greenhouse) {
        this(id, inventory, new ClientState(greenhouse, greenhouse.seedHandler(), greenhouse.outputHandler(),
                greenhouse.cardHandler(), greenhouse.getBlockPos(), greenhouseData(greenhouse)));
    }

    /** Client side constructor. */
    public OmniGreenhouseMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, readClient(inventory, buf));
    }

    private OmniGreenhouseMenu(int id, Inventory inventory, ClientState state) {
        super(ModMenus.OMNI_GREENHOUSE.get(), id);
        this.greenhouse = state.greenhouse();
        this.pos = state.pos();
        this.data = state.data();

        addSlot(new SlotItemHandler(state.seed(), 0, 60, 24));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(state.outputs(), row * 3 + column, 114 + column * 18, 28 + row * 18));
            }
        }
        addSlot(new SlotItemHandler(state.cards(), 0, 64, 64));
        addSlot(new SlotItemHandler(state.cards(), 1, 82, 64));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PLAYER_INVENTORY_X + column * 18, HOTBAR_Y));
        }
        addDataSlots(data);
    }

    private static ClientState readClient(Inventory inventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        OmniGreenhouseBlockEntity greenhouse = inventory.player.level().getBlockEntity(pos)
                instanceof OmniGreenhouseBlockEntity found ? found : null;
        IItemHandler seed = greenhouse != null ? greenhouse.seedHandler() : new ItemStackHandler(1);
        IItemHandler outputs = greenhouse != null ? greenhouse.outputHandler()
                : new ItemStackHandler(OmniGreenhouseBlockEntity.OUTPUT_SLOTS);
        IItemHandler cards = greenhouse != null ? greenhouse.cardHandler()
                : new ItemStackHandler(OmniGreenhouseBlockEntity.CARD_SLOTS);
        return new ClientState(greenhouse, seed, outputs, cards, pos, new SimpleContainerData(DATA_COUNT));
    }

    private static ContainerData greenhouseData(OmniGreenhouseBlockEntity greenhouse) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> greenhouse.energyStorage().getEnergyStored();
                    case 1 -> greenhouse.capacity();
                    case 2 -> greenhouse.tank().getFluidAmount();
                    case 3 -> greenhouse.tank().getCapacity();
                    case 4 -> greenhouse.progress();
                    case 5 -> greenhouse.period();
                    case 6 -> greenhouse.activeInput() ? 1 : 0;
                    case 7 -> greenhouse.activeOutput() ? 1 : 0;
                    default -> index >= 8 && index < 14
                            ? greenhouse.faceMode(Direction.values()[index - 8]).ordinal() : 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // The server is the only writer.
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    @Nullable
    public OmniGreenhouseBlockEntity greenhouse() {
        return greenhouse;
    }

    public BlockPos blockPos() {
        return pos;
    }

    public int energyStored() {
        return data.get(0);
    }

    public int energyCapacity() {
        return Math.max(1, data.get(1));
    }

    public int fluidAmount() {
        return data.get(2);
    }

    public int fluidCapacity() {
        return Math.max(1, data.get(3));
    }

    public int progress() {
        return data.get(4);
    }

    public int period() {
        return Math.max(1, data.get(5));
    }

    public boolean activeInput() {
        return data.get(6) != 0;
    }

    public boolean activeOutput() {
        return data.get(7) != 0;
    }

    public GreenhouseFaceMode faceMode(int guiFaceIndex) {
        Direction direction = guiDirection(guiFaceIndex);
        return GreenhouseFaceMode.byOrdinal(data.get(8 + direction.get3DDataValue()));
    }

    /** Maps a GUI button index to a world direction, honouring the block's facing. */
    public Direction guiDirection(int guiFaceIndex) {
        Direction front = Direction.NORTH;
        if (greenhouse != null) {
            front = greenhouse.getBlockState().getValue(xiabis.modpack.toolbox.block.OmniGreenhouseBlock.FACING);
        }
        return switch (guiFaceIndex) {
            case 0 -> Direction.UP;
            case 1 -> front.getCounterClockWise();
            case 2 -> front;
            case 3 -> front.getClockWise();
            case 4 -> front.getOpposite();
            default -> Direction.DOWN;
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, MACHINE_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.OMNI_GREENHOUSE.get());
    }

    private record ClientState(@Nullable OmniGreenhouseBlockEntity greenhouse, IItemHandler seed, IItemHandler outputs,
                               IItemHandler cards, BlockPos pos, ContainerData data) {
    }
}
