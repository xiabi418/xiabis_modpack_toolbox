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
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import xiabis.modpack.toolbox.registry.ModBlockEntities;
import xiabis.modpack.toolbox.util.BatteryFaceMode;

/**
 * Stores energy for the omni battery box. The capacity is {@link Integer#MAX_VALUE}, which is the largest amount
 * the Forge Energy API can address, so it behaves as an unbounded buffer in practice.
 *
 * <p>Every side is either an input or an output side; by default all six accept energy. With active I/O enabled
 * the block also pushes energy to output sides and pulls it from input sides once per tick.
 */
public class OmniBatteryBlockEntity extends BlockEntity {
    /** Largest amount of FE the API can represent. */
    public static final int CAPACITY = Integer.MAX_VALUE;
    /** How much energy active I/O moves per side and tick. */
    public static final int TRANSFER_PER_TICK = 1_000_000;

    private final BatteryEnergyStorage storage = new BatteryEnergyStorage();
    private final BatteryFaceMode[] faces = new BatteryFaceMode[6];
    private final IEnergyStorage[] faceViews = new IEnergyStorage[6];
    private boolean activeIo;

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

    public int storedEnergy() {
        return this.storage.getEnergyStored();
    }

    /** The storage a neighbour sees from the given side; {@code null} is the generic, two way view. */
    public IEnergyStorage energyHandler(Direction side) {
        return side == null ? this.storage : this.faceViews[side.get3DDataValue()];
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
        if (this.storage.getEnergyStored() <= 0) {
            return;
        }
        IEnergyStorage target = this.neighbour(direction);
        if (target == null) {
            return;
        }
        int moved = target.receiveEnergy(Math.min(TRANSFER_PER_TICK, this.storage.getEnergyStored()), true);
        if (moved <= 0) {
            return;
        }
        int extracted = this.storage.extractEnergy(moved, false);
        target.receiveEnergy(extracted, false);
        this.setChanged();
    }

    private void pull(Direction direction) {
        if (this.storage.getEnergyStored() >= CAPACITY) {
            return;
        }
        IEnergyStorage source = this.neighbour(direction);
        if (source == null || !source.canExtract()) {
            return;
        }
        int room = CAPACITY - this.storage.getEnergyStored();
        int moved = source.extractEnergy(Math.min(TRANSFER_PER_TICK, room), true);
        if (moved <= 0) {
            return;
        }
        int accepted = this.storage.receiveEnergy(moved, false);
        source.extractEnergy(accepted, false);
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
        tag.putInt("Energy", this.storage.getEnergyStored());
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
        this.storage.setStored(tag.getInt("Energy"));
        this.activeIo = tag.getBoolean("ActiveIo");
        byte[] modes = tag.getByteArray("Faces");
        for (Direction direction : Direction.values()) {
            int index = direction.get3DDataValue();
            this.faces[index] = index < modes.length
                    ? BatteryFaceMode.byOrdinal(modes[index])
                    : BatteryFaceMode.INPUT;
        }
    }

    private static final class BatteryEnergyStorage extends EnergyStorage {
        private BatteryEnergyStorage() {
            super(CAPACITY, CAPACITY, CAPACITY, 0);
        }

        private void setStored(int amount) {
            this.energy = Math.clamp(amount, 0, this.capacity);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int accepted = Math.min(maxReceive, this.capacity - this.energy);
            if (!simulate) {
                this.energy += accepted;
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(maxExtract, this.energy);
            if (!simulate) {
                this.energy -= extracted;
            }
            return extracted;
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
                    ? OmniBatteryBlockEntity.this.storage.receiveEnergy(maxReceive, simulate)
                    : 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return OmniBatteryBlockEntity.this.mode(this.direction) == BatteryFaceMode.OUTPUT
                    ? OmniBatteryBlockEntity.this.storage.extractEnergy(maxExtract, simulate)
                    : 0;
        }

        @Override
        public int getEnergyStored() {
            return OmniBatteryBlockEntity.this.storage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return CAPACITY;
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
