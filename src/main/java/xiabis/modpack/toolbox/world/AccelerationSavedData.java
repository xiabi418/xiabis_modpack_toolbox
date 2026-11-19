package xiabis.modpack.toolbox.world;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per level store of the blocks that are currently being accelerated by the time wand.
 *
 * <p>Each entry remembers the acceleration tier and the game time at which the acceleration runs
 * out. The multiplier list is the one from the design document: 4x, 8x, 16x, 64x, 128x, 256x.
 */
public final class AccelerationSavedData extends SavedData {
    public static final String FILE_ID = "xmt_time_acceleration";

    /** One acceleration lasts 60 seconds; every upgrade resets this timer. */
    public static final int DURATION_TICKS = 60 * 20;

    public static final int[] MULTIPLIERS = {4, 8, 16, 64, 128, 256};

    public record Entry(int tier, long endTick) {
        public int multiplier() {
            return MULTIPLIERS[Math.max(0, Math.min(this.tier, MULTIPLIERS.length - 1))];
        }

        public int remainingTicks(long now) {
            return (int) Math.max(0L, this.endTick - now);
        }
    }

    private final Map<BlockPos, Entry> entries = new HashMap<>();

    public static AccelerationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(AccelerationSavedData::new, AccelerationSavedData::load), FILE_ID);
    }

    public static AccelerationSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        AccelerationSavedData data = new AccelerationSavedData();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"));
            data.entries.put(pos, new Entry(entry.getInt("Tier"), entry.getLong("End")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, Entry> entry : this.entries.entrySet()) {
            CompoundTag nbt = new CompoundTag();
            BlockPos pos = entry.getKey();
            nbt.putInt("X", pos.getX());
            nbt.putInt("Y", pos.getY());
            nbt.putInt("Z", pos.getZ());
            nbt.putInt("Tier", entry.getValue().tier());
            nbt.putLong("End", entry.getValue().endTick());
            list.add(nbt);
        }
        tag.put("Entries", list);
        return tag;
    }

    public Map<BlockPos, Entry> entries() {
        return this.entries;
    }

    public Entry get(BlockPos pos) {
        return this.entries.get(pos);
    }

    public void put(BlockPos pos, Entry entry) {
        this.entries.put(pos.immutable(), entry);
        this.setDirty();
    }

    public void remove(BlockPos pos) {
        if (this.entries.remove(pos) != null) {
            this.setDirty();
        }
    }

    private AccelerationSavedData() {
    }
}
