package xiabis.modpack.toolbox.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.item.MarkedEffects;

public final class ModComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, XmtMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_MODE =
            COMPONENTS.registerComponentType("tool_mode",
                    b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SILK_MODE =
            COMPONENTS.registerComponentType("silk_mode",
                    b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> STATE_APPLIED =
            COMPONENTS.registerComponentType("state_applied",
                    b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MarkedEffects>> MARKED_EFFECTS =
            COMPONENTS.registerComponentType("marked_effects",
                    b -> b.persistent(MarkedEffects.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(MarkedEffects.CODEC))
                            .cacheEncoding());

    /** FE stored inside a giant battery box item (long, since the block itself stores up to Long.MAX_VALUE). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> STORED_ENERGY =
            COMPONENTS.registerComponentType("stored_energy",
                    b -> b.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).cacheEncoding());

    private ModComponents() {
    }
}
