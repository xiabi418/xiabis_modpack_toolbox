package xiabis.modpack.toolbox.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import xiabis.modpack.toolbox.XmtMod;

/** Registers the codec referenced by the server-side dimension JSON files. */
public final class ModWorldgen {
    private ModWorldgen() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModWorldgen::registerChunkGenerator);
    }

    private static void registerChunkGenerator(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.CHUNK_GENERATOR)) {
            event.register(Registries.CHUNK_GENERATOR,
                    helper -> helper.register(ResourceLocation.fromNamespaceAndPath(XmtMod.MOD_ID, "private_dimension"),
                            PrivateDimensionChunkGenerator.CODEC));
        }
    }
}
