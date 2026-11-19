package xiabis.modpack.toolbox.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xiabis.modpack.toolbox.XmtMod;

public record SetToolModePayload(int mode, boolean silk) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetToolModePayload> TYPE =
            new CustomPacketPayload.Type<>(XmtMod.id("set_tool_mode"));

    public static final StreamCodec<ByteBuf, SetToolModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetToolModePayload::mode,
            ByteBufCodecs.BOOL, SetToolModePayload::silk,
            SetToolModePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
