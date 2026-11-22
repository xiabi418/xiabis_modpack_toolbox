package xiabis.modpack.toolbox.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import xiabis.modpack.toolbox.XmtMod;

/** Places a single item (usually a block item dragged out of JEI) into one marker slot. */
public record SetArmorMarkerPayload(int slot, ItemStack stack) implements CustomPacketPayload {
    public static final Type<SetArmorMarkerPayload> TYPE = new Type<>(XmtMod.id("set_armor_marker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetArmorMarkerPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SetArmorMarkerPayload::slot,
                    ItemStack.OPTIONAL_STREAM_CODEC, SetArmorMarkerPayload::stack,
                    SetArmorMarkerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
