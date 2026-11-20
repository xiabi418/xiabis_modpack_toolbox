package xiabis.modpack.toolbox.blockentity;

import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import xiabis.modpack.toolbox.registry.ModBlockEntities;
import xiabis.modpack.toolbox.util.BatteryFaceMode;

/**
 * Stores energy for the omni battery box in a {@code long}, so the buffer can hold up to
 * {@link Long#MAX_VALUE} FE (~9.22e18). The Forge Energy API itself is {@code int} based, so every interaction
 * with a neighbour is clamped to {@code Integer.MAX_VALUE} per call.
 *
 * <p>Every side is either an input or an output side; by default all six accept energy. With active I/O enabled
 * the block also pushes energy to output sides and pulls it from input sides once per tick.
 */
public class OmniBatteryBlockEntity extends BlockEntity {
    /** Internal capacity: as much as a {@code long} can hold. */
    public static final long CAPACITY = Long.MAX_VALUE;
    /** How much energy active I/O moves per side and tick (2.1 billion FE). */
    public static final int TRANSFER_PER_TICK = 2_100_000_000;

    private final BatteryFaceMode[] faces = new BatteryFaceMode[6];
    private final IEnergyStorage[] faceViews = new IEnergyStorage[6];
    private final IEnergyStorage genericView = new GenericEnergyStorage();
    private boolean activeIo;
    private long energy;

    public OmniBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OMNI_BATTERY.get(), pos, state);
        Arrays.fill(this.faces, BatteryFaceMode.INPUT);
        for (Direction direction : Direction.values()) {
            this.faceViews[direction.get3DDataValue()] = new FaceEnergyStorage(direction);
        }
    }

    public BatteryFaceMode mode(Direction direction) {
        return this.faces[direction.get3DDataValue()];
    }

    public void cycle(Direction direction) {
        int index = direction.get3DDataValue();
        this.faces[index] = this.faces[index].next();
        this.setChanged();
    }

    public boolean activeIo() {
        return this.activeIo;
    }

    public void toggleActiveIo() {
        this.activeIo = !this.activeIo;
        this.setChanged();
    }

    public long storedEnergy() {
        return this.energy;
    }

    /** Restores the charge when the block is placed from a battery item. */
    public void setStoredEnergy(long amount) {
        this.energy = Math.clamp(amount, 0L, CAPACITY);
        this.setChanged();
    }

    private long receiveEnergy(long maxReceive, boolean simulate) {
        long accepted = Math.min(maxReceive, CAPACITY - this.energy);
        if (accepted <= 0) {
            return 0L;
        }
        if (!simulate) {
            this.energy += accepted;
        }
        return accepted;
    }

    private long extractEnergy(long maxExtract, boolean simulate) {
        long extracted = Math.min(maxExtract, this.energy);
        if (extracted <= 0) {
            return 0L;
        }
        if (!simulate) {
            this.energy -= extracted;
        }
        return extracted;
    }

    /** The storage a neighbour sees from the given side; {@code null} is the generic, two way view. */
    public IEnergyStorage energyHandler(Direction side) {
        return side == null ? this.genericView : this.faceViews[side.get3DDataValue()];
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OmniBatteryBlockEntity battery) {
        if (!battery.activeIo) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (battery.mode(direction) == BatteryFaceMode.OUTPUT) {
                battery.push(direction);
            } else {
                battery.pull(direction);
            }
        }
    }

    private void push(Direction direction) {
        if (this.energy <= 0L) {
            return;
        }
        IEnergyStorage target = this.neighbour(direction);
        if (target == null || !target.canReceive()) {
            return;
        }
        int offer = (int) Math.min(TRANSFER_PER_TICK, this.energy);
        int accepted = target.receiveEnergy(offer, true);
        if (accepted <= 0) {
            return;
        }
        long extracted = this.extractEnergy(accepted, false);
        target.receiveEnergy((int) extracted, false);
        this.setChanged();
    }

    private void pull(Direction direction) {
        if (this.energy >= CAPACITY) {
            return;
        }
        IEnergyStorage source = this.neighbour(direction);
        if (source == null || !source.canExtract()) {
            return;
        }
        int request = (int) Math.min(TRANSFER_PER_TICK, CAPACITY - this.energy);
        int offered = source.extractEnergy(request, true);
        if (offered <= 0) {
            return;
        }
        long accepted = this.receiveEnergy(offered, false);
        source.extractEnergy((int) accepted, false);
        this.setChanged();
    }

    private IEnergyStorage neighbour(Direction direction) {
        if (this.level == null) {
            return null;
        }
        BlockPos target = this.worldPosition.relative(direction);
        if (!this.level.isLoaded(target)) {
            return null;
        }
        return this.level.getCapability(Capabilities.EnergyStorage.BLOCK, target, direction.getOpposite());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("Energy", this.energy);
        tag.putBoolean("ActiveIo", this.activeIo);
        byte[] modes = new byte[6];
        for (Direction direction : Direction.values()) {
            modes[direction.get3DDataValue()] = (byte) this.mode(direction).ordinal();
        }
        tag.putByteArray("Faces", modes);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energy = Math.clamp(tag.getLong("Energy"), 0L, CAPACITY);
        this.activeIo = tag.getBoolean("ActiveIo");
        byte[] modes = tag.getByteArray("Faces");
        for (Direction direction : Direction.values()) {
            int index = direction.get3DDataValue();
            this.faces[index] = index < modes.length
                    ? BatteryFaceMode.byOrdinal(modes[index])
                    : BatteryFaceMode.INPUT;
        }
    }

    /** Two way view used for side-less capability lookups. */
    private final class GenericEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return (int) OmniBatteryBlockEntity.this.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return (int) OmniBatteryBlockEntity.this.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, OmniBatteryBlockEntity.this.energy);
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private final class FaceEnergyStorage implements IEnergyStorage {
        private final Direction direction;

        private FaceEnergyStorage(Direction direction) {
            this.direction = direction;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return OmniBatteryBlockEntity.this.mode(this.direction) == BatteryFaceMode.INPUT
                    ? (int) OmniBatteryBlockEntity.this.receiveEnergy(maxReceive, simulate)
                    : 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return OmniBatteryBlockEntity.this.mode(this.direction) == BatteryFaceMode.OUTPUT
                    ? (int) OmniBatteryBlockEntity.this.extractEnergy(maxExtract, simulate)
                    : 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, OmniBatteryBlockEntity.this.energy);
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return OmniBatteryBlockEntity.this.mode(this.direction) == BatteryFaceMode.OUTPUT;
        }

        @Override
        public boolean canReceive() {
            return OmniBatteryBlockEntity.this.mode(this.direction) == BatteryFaceMode.INPUT;
        }
    }
}
