package xiabis.modpack.toolbox.worldgen;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import xiabis.modpack.toolbox.XmtMod;

/** Each player keeps their latest arrival origin separately for each shared destination. */
public record DimensionReturnPoint(ResourceLocation dimension, double x, double y, double z, float yaw, float pitch) {
    public static final Codec<DimensionReturnPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(DimensionReturnPoint::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(DimensionReturnPoint::x),
            Codec.DOUBLE.fieldOf("y").forGetter(DimensionReturnPoint::y),
            Codec.DOUBLE.fieldOf("z").forGetter(DimensionReturnPoint::z),
            Codec.FLOAT.fieldOf("yaw").forGetter(DimensionReturnPoint::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(DimensionReturnPoint::pitch)
    ).apply(instance, DimensionReturnPoint::new));

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, XmtMod.MOD_ID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Map<ResourceLocation, DimensionReturnPoint>>>
            RETURN_POINTS = ATTACHMENTS.register("dimension_return_points", () -> AttachmentType
                    .<Map<ResourceLocation, DimensionReturnPoint>>builder(() -> Map.of())
                    .serialize(Codec.unboundedMap(ResourceLocation.CODEC, CODEC)).copyOnDeath().build());

    public static DimensionReturnPoint capture(ServerPlayer player) {
        return new DimensionReturnPoint(player.serverLevel().dimension().location(), player.getX(), player.getY(),
                player.getZ(), player.getYRot(), player.getXRot());
    }

    public void remember(ServerPlayer player, ResourceKey<Level> arrival) {
        var points = new HashMap<>(player.getData(RETURN_POINTS));
        points.put(arrival.location(), this);
        player.setData(RETURN_POINTS, Map.copyOf(points));
    }

    public static DimensionReturnPoint forCurrentDimension(ServerPlayer player) {
        return player.getData(RETURN_POINTS).get(player.serverLevel().dimension().location());
    }

    public boolean teleport(ServerPlayer player) {
        ServerLevel destination = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        if (destination == null) {
            return false;
        }
        player.teleportTo(destination, x, y, z, yaw, pitch);
        return player.serverLevel() == destination;
    }
}
