package xiabis.modpack.toolbox.blockentity;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xiabis.modpack.toolbox.block.DimensionTeleportBlock;
import xiabis.modpack.toolbox.registry.ModBlockEntities;

public final class ReturnDimensionBlockEntity extends BlockEntity {
    private UUID owner;
    private ResourceKey<Level> dimension;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public ReturnDimensionBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RETURN_DIMENSION.get(), pos, state);
    }

    public void setReturnPoint(ServerPlayer player, ServerLevel origin) {
        owner = player.getUUID();
        dimension = origin.dimension();
        x = player.getX();
        y = player.getY();
        z = player.getZ();
        yaw = player.getYRot();
        pitch = player.getXRot();
        setChanged();
    }

    public boolean returnPlayer(ServerPlayer player) {
        if (owner == null || !owner.equals(player.getUUID()) || dimension == null) {
            return false;
        }
        ServerLevel destination = player.getServer().getLevel(dimension);
        if (destination == null) {
            return false;
        }
        player.teleportTo(destination, x, y, z, yaw, pitch);
        return player.serverLevel() == destination;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null && dimension != null) {
            tag.putUUID("Owner", owner);
            tag.putString("Dimension", dimension.location().toString());
            tag.putDouble("X", x);
            tag.putDouble("Y", y);
            tag.putDouble("Z", z);
            tag.putFloat("Yaw", yaw);
            tag.putFloat("Pitch", pitch);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ResourceLocation location = ResourceLocation.tryParse(tag.getString("Dimension"));
        dimension = location == null ? null : ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, location);
        x = tag.getDouble("X");
        y = tag.getDouble("Y");
        z = tag.getDouble("Z");
        yaw = tag.getFloat("Yaw");
        pitch = tag.getFloat("Pitch");
    }
}
