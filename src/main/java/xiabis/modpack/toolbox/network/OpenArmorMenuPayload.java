package xiabis.modpack.toolbox.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xiabis.modpack.toolbox.XmtMod;

/** Client asks the server to open the omni armor marker menu for the armor it is wearing/holding. */
public record OpenArmorMenuPayload() implements CustomPacketPayload {
    public static final Type<OpenArmorMenuPayload> TYPE = new Type<>(XmtMod.id("open_armor_menu"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenArmorMenuPayload> CODEC =
            StreamCodec.unit(new OpenArmorMenuPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
