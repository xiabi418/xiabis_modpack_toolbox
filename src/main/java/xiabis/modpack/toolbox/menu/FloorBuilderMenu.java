package xiabis.modpack.toolbox.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import xiabis.modpack.toolbox.registry.ModBlocks;
import xiabis.modpack.toolbox.registry.ModMenus;

public final class FloorBuilderMenu extends AbstractContainerMenu {
    public static final int BORDER = 0, FILL = 1, GLOW = 2, SIZE = 3, REPLACE = 4, BUILD = 5;
    private final SimpleContainerData options = new SimpleContainerData(BUILD);
    private final BlockPos origin;
    private final Level openedLevel;

    public FloorBuilderMenu(int id, Inventory inventory, BlockPos origin) {
        super(ModMenus.FLOOR_BUILDER.get(), id);
        this.origin = origin.immutable();
        this.openedLevel = inventory.player.level();
        addDataSlots(options);
    }

    public int option(int index) { return options.get(index); }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return player.level() == openedLevel && openedLevel.hasChunkAt(origin)
                && stillValid(ContainerLevelAccess.create(openedLevel, origin), player, ModBlocks.FLOOR_BUILDER.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player) || !player.getAbilities().mayBuild) return false;
        switch (id) {
            case BORDER, FILL -> options.set(id, (option(id) + 1) % DyeColor.values().length);
            case SIZE -> options.set(id, (option(id) + 1) % 3);
            case GLOW, REPLACE -> options.set(id, 1 - option(id));
            case BUILD -> {
                if (build(serverPlayer)) serverPlayer.closeContainer();
            }
            default -> { return false; }
        }
        return true;
    }

    private boolean build(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ChunkPos center = new ChunkPos(origin);
        int radius = option(SIZE);
        for (int x = center.x - radius; x <= center.x + radius; x++) {
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                if (level.getChunkSource().getChunkNow(x, z) == null) return fail(player, "unloaded");
            }
        }
        var palette = option(GLOW) != 0 ? ModBlocks.GLOWING_POLISHED_CONCRETE : ModBlocks.POLISHED_CONCRETE;
        BlockState border = palette.get(DyeColor.byId(option(BORDER))).get().defaultBlockState();
        BlockState fill = palette.get(DyeColor.byId(option(FILL))).get().defaultBlockState();
        boolean replace = option(REPLACE) != 0;
        BlockPos min = new BlockPos((center.x - radius) * 16, origin.getY(), (center.z - radius) * 16);
        BlockPos max = min.offset((2 * radius + 1) * 16 - 1, 0, (2 * radius + 1) * 16 - 1);

        // Validate the whole area before opening any floor cells or consuming the builder.
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!level.mayInteract(player, pos)) return fail(player, "protected");
            if (pos.equals(origin)) continue;
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state == floorAt(pos, border, fill)) continue;
            if (!replace) return fail(player, "occupied");
            if (!canRemove(level, pos, state) || NeoForge.EVENT_BUS.post(
                    new BlockEvent.BreakEvent(level, pos.immutable(), state, player)).isCanceled()) {
                return fail(player, "protected");
            }
        }

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (pos.equals(origin)) continue;
            BlockState floor = floorAt(pos, border, fill);
            BlockState state = level.getBlockState(pos);
            if (state == floor) continue;
            if (!state.isAir() && (!replace || !canRemove(level, pos, state)
                    || !level.destroyBlock(pos, true, player, 512))) return fail(player, "interrupted");
            if (!level.setBlock(pos, floor, Block.UPDATE_ALL)) return fail(player, "interrupted");
        }
        return level.setBlock(origin, floorAt(origin, border, fill), Block.UPDATE_ALL) || fail(player, "interrupted");
    }

    private static boolean canRemove(ServerLevel level, BlockPos pos, BlockState state) {
        return state.getDestroySpeed(level, pos) >= 0 && !(state.getBlock() instanceof GameMasterBlock);
    }

    static BlockState floorAt(BlockPos pos, BlockState border, BlockState fill) {
        int x = pos.getX() & 15, z = pos.getZ() & 15;
        return x == 0 || x == 15 || z == 0 || z == 15 ? border : fill;
    }

    private static boolean fail(ServerPlayer player, String reason) {
        player.displayClientMessage(Component.translatable("menu.xiabis_modpack_toolbox.floor_builder." + reason), true);
        return false;
    }
}
