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
import xiabis.modpack.toolbox.blockentity.OmniGeneratorBlockEntity;
import xiabis.modpack.toolbox.registry.ModBlocks;
import xiabis.modpack.toolbox.registry.ModMenus;
import xiabis.modpack.toolbox.util.GeneratorEnergyType;
import xiabis.modpack.toolbox.util.GeneratorFaceMode;

/** Container of the omni generator: 9 fuel slots, 2 card slots, the player inventory and the GUI state. */
public class OmniGeneratorMenu extends AbstractContainerMenu {
    /** energy, capacity, burn, burnTotal, activeInput, activeOutput, type, 6 faces, fluid, fluidCapacity. */
    public static final int DATA_COUNT = 15;

    private static final int PLAYER_INVENTORY_X = 25;
    private static final int PLAYER_INVENTORY_Y = 112;

    @Nullable
    private final OmniGeneratorBlockEntity generator;
    private final BlockPos pos;
    private final ContainerData data;

    /** Server side constructor. */
    public OmniGeneratorMenu(int id, Inventory inventory, OmniGeneratorBlockEntity generator) {
        this(id, inventory, new ClientState(generator, generator.fuelHandler(), generator.cardHandler(),
                generator.getBlockPos(), generatorData(generator)));
    }

    /** Client side constructor. */
    public OmniGeneratorMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, readClient(inventory, buf));
    }

    private OmniGeneratorMenu(int id, Inventory inventory, ClientState state) {
        super(ModMenus.OMNI_GENERATOR.get(), id);
        this.generator = state.generator();
        this.pos = state.pos();
        this.data = state.data();

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(state.fuel(), row * 3 + column, 82 + column * 20, 18 + row * 20));
            }
        }
        addSlot(new SlotItemHandler(state.cards(), 0, 194, 18));
        addSlot(new SlotItemHandler(state.cards(), 1, 194, 42));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + 58));
        }
        addDataSlots(data);
    }

    private static ClientState readClient(Inventory inventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        OmniGeneratorBlockEntity generator = inventory.player.level().getBlockEntity(pos)
                instanceof OmniGeneratorBlockEntity found ? found : null;
        IItemHandler fuel = generator != null ? generator.fuelHandler()
                : new ItemStackHandler(OmniGeneratorBlockEntity.FUEL_SLOTS);
        IItemHandler cards = generator != null ? generator.cardHandler() : new ItemStackHandler(2);
        return new ClientState(generator, fuel, cards, pos, new SimpleContainerData(DATA_COUNT));
    }

    private static ContainerData generatorData(OmniGeneratorBlockEntity generator) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> generator.energyStorage().getEnergyStored();
                    case 1 -> generator.capacity();
                    case 2 -> generator.burnTime();
                    case 3 -> generator.burnTotal();
                    case 4 -> generator.activeInput() ? 1 : 0;
                    case 5 -> generator.activeOutput() ? 1 : 0;
                    case 6 -> generator.energyType().ordinal();
                    case 13 -> generator.tank().getFluidAmount();
                    case 14 -> generator.tank().getCapacity();
                    default -> index >= 7 && index < 13
                            ? generator.faceMode(Direction.values()[index - 7]).ordinal() : 0;
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
    public OmniGeneratorBlockEntity generator() {
        return generator;
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

    public int burnTime() {
        return data.get(2);
    }

    public int burnTotal() {
        return Math.max(1, data.get(3));
    }

    public boolean activeInput() {
        return data.get(4) != 0;
    }

    public boolean activeOutput() {
        return data.get(5) != 0;
    }

    public GeneratorEnergyType energyType() {
        return GeneratorEnergyType.byOrdinal(data.get(6));
    }

    public GeneratorFaceMode faceMode(int guiFaceIndex) {
        Direction direction = guiDirection(guiFaceIndex);
        return GeneratorFaceMode.byOrdinal(data.get(7 + direction.get3DDataValue()));
    }

    /** Maps a GUI button index to a world direction, honouring the block's facing. */
    public Direction guiDirection(int guiFaceIndex) {
        Direction front = Direction.NORTH;
        if (generator != null) {
            front = generator.getBlockState().getValue(xiabis.modpack.toolbox.block.OmniGeneratorBlock.FACING);
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

    public int fluidAmount() {
        return data.get(13);
    }

    public int fluidCapacity() {
        return Math.max(1, data.get(14));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineSlots = OmniGeneratorBlockEntity.FUEL_SLOTS + 2;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, machineSlots, false)) {
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
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.OMNI_GENERATOR.get());
    }

    private record ClientState(@Nullable OmniGeneratorBlockEntity generator, IItemHandler fuel, IItemHandler cards,
                               BlockPos pos, ContainerData data) {
    }
}
