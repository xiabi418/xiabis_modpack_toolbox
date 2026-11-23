package xiabis.modpack.toolbox.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xiabis.modpack.toolbox.XmtMod;

public record CondenserConfigPayload(int action) implements CustomPacketPayload {
    public static final Type<CondenserConfigPayload> TYPE = new Type<>(XmtMod.id("condenser_config"));
    public static final int TOGGLE_OUTPUT = 0;
    public static final int TOGGLE_MODE = 1;
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CondenserConfigPayload> CODEC =
            StreamCodec.of((buffer, payload) -> buffer.writeVarInt(payload.action()),
                    buffer -> new CondenserConfigPayload(buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
