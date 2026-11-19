package xiabis.modpack.toolbox.network;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xiabis.modpack.toolbox.XmtMod;

/**
 * A GUI click on the omni generator screen.
 *
 * <p>{@code action}: 0 = toggle active output, 1 = toggle active input, 2 = cycle the energy type,
 * 3..8 = cycle the face mode of GUI face {@code action - 3}. {@code value} is +1 for a left click and
 * -1 for a right click.</p>
 */
public record GeneratorConfigPayload(int action, int value) implements CustomPacketPayload {
    public static final int ACTION_ACTIVE_OUTPUT = 0;
    public static final int ACTION_ACTIVE_INPUT = 1;
    public static final int ACTION_ENERGY_TYPE = 2;
    public static final int ACTION_FACE_BASE = 3;

    public static final Type<GeneratorConfigPayload> TYPE = new Type<>(XmtMod.id("generator_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GeneratorConfigPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, GeneratorConfigPayload::action,
                    ByteBufCodecs.VAR_INT, GeneratorConfigPayload::value,
                    GeneratorConfigPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static GeneratorConfigPayload face(int guiFaceIndex, int direction) {
        return new GeneratorConfigPayload(ACTION_FACE_BASE + guiFaceIndex, direction);
    }

    /** Convenience for the screen: the face direction expressed as a short label. */
    public static String faceLetter(Direction direction) {
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
