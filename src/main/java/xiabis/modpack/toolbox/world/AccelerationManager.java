package xiabis.modpack.toolbox.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Server side logic of the time wand.
 *
 * <p>Acceleration follows the approach of "Time in a Bottle" / "Just Dire Things": every game tick
 * the block entity ticker (or the block's random tick) is executed {@code multiplier - 1} extra
 * times on top of the vanilla tick, so a "4x" block runs four times per tick in total.
 */
public final class AccelerationManager {
    /** Safety valve so a few 256x blocks cannot stall the server; total extra ticks per level tick. */
    private static final int EXTRA_TICK_BUDGET = 2_048;

    private static final double[][] FACE_NORMALS = {
            {0.52D, 0.0D, 0.0D}, {-0.52D, 0.0D, 0.0D},
            {0.0D, 0.52D, 0.0D}, {0.0D, -0.52D, 0.0D},
            {0.0D, 0.0D, 0.52D}, {0.0D, 0.0D, -0.52D}
    };

    /** True when the wand can speed this block up: it has a ticking block entity or random ticks. */
    public static boolean canAccelerate(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            return tickerFor(level, state, blockEntity) != null;
        }
        return state.isRandomlyTicking();
    }

    public static void tick(ServerLevel level) {
        AccelerationSavedData data = AccelerationSavedData.get(level);
        if (data.entries().isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        List<BlockPos> expired = new ArrayList<>();
        int budget = EXTRA_TICK_BUDGET;
        for (Map.Entry<BlockPos, AccelerationSavedData.Entry> entry : data.entries().entrySet()) {
            BlockPos pos = entry.getKey();
            AccelerationSavedData.Entry value = entry.getValue();
            if (now >= value.endTick() || !level.isLoaded(pos) || !canAccelerate(level, pos)) {
                expired.add(pos);
                continue;
            }
            // The wand creates the display immediately. Refreshing it once per second is enough for the
            // countdown and avoids an entity scan for every accelerated block on every server tick.
            if (now % 20L == 0L) {
                float remainingTicks = value.remainingTicks(now);
                AccelerationDisplay.sync(level, pos, value.tier(),
                        Math.max(0, Mth.ceil(remainingTicks / 20.0F)), true);
            }
            int extra = Math.min(value.multiplier() - 1, budget);
            budget -= extra;
            if (extra > 0) {
                accelerate(level, pos, extra);
            }
            if (budget <= 0) {
                break;
            }
        }
        for (BlockPos pos : expired) {
            data.remove(pos);
            AccelerationDisplay.remove(level, pos);
        }
    }

    /** Runs {@code times} additional ticks on a single block. */
    public static void accelerate(ServerLevel level, BlockPos pos, int times) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            BlockEntityTicker<BlockEntity> ticker = tickerFor(level, state, blockEntity);
            if (ticker == null) {
                return;
            }
            for (int i = 0; i < times; i++) {
                ticker.tick(level, pos, state, blockEntity);
            }
        } else if (state.isRandomlyTicking()) {
            for (int i = 0; i < times; i++) {
                state.randomTick(level, pos, level.random);
            }
        }
    }

    /** Sparks on all six block faces so an accelerated block is visible from every side. */
    public static void spawnParticles(ServerLevel level) {
        AccelerationSavedData data = AccelerationSavedData.get(level);
        if (data.entries().isEmpty()) {
            return;
        }
        for (BlockPos pos : data.entries().keySet()) {
            if (!level.isLoaded(pos)) {
                continue;
            }
            for (double[] normal : FACE_NORMALS) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5D + normal[0],
                        pos.getY() + 0.5D + normal[1],
                        pos.getZ() + 0.5D + normal[2],
                        1, 0.05D, 0.05D, 0.05D, 0.0D);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockEntityTicker<BlockEntity> tickerFor(ServerLevel level, BlockState state,
            BlockEntity blockEntity) {
        BlockEntityType<?> type = blockEntity.getType();
        return (BlockEntityTicker<BlockEntity>) (BlockEntityTicker) state.getTicker(level,
                (BlockEntityType) type);
    }

    private AccelerationManager() {
    }
}
