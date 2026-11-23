package xiabis.modpack.toolbox.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import xiabis.modpack.toolbox.XmtMod;

/**
 * A GUI click on the omni greenhouse screen.
 *
 * <p>{@code action}: 0 = toggle active output, 1 = toggle active input, 2..7 = cycle the face mode of
 * GUI face {@code action - 2}. {@code value} is +1 for a left click and -1 for a right click.</p>
 */
public record GreenhouseConfigPayload(int action, int value) implements CustomPacketPayload {
    public static final int ACTION_ACTIVE_OUTPUT = 0;
    public static final int ACTION_ACTIVE_INPUT = 1;
    public static final int ACTION_FACE_BASE = 2;

    public static final Type<GreenhouseConfigPayload> TYPE = new Type<>(XmtMod.id("greenhouse_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GreenhouseConfigPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, GreenhouseConfigPayload::action,
                    ByteBufCodecs.VAR_INT, GreenhouseConfigPayload::value,
                    GreenhouseConfigPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static GreenhouseConfigPayload face(int guiFaceIndex, int direction) {
        return new GreenhouseConfigPayload(ACTION_FACE_BASE + guiFaceIndex, direction);
    }

    public static String faceLetter(net.minecraft.core.Direction direction) {
        return switch (direction) {
            case UP -> "U";
            case DOWN -> "D";
            case NORTH -> "N";
            case SOUTH -> "S";
            case WEST -> "W";
            case EAST -> "E";
        };
    }
}
