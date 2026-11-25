package xiabis.modpack.toolbox;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xiabis.modpack.toolbox.network.ModNetwork;
import xiabis.modpack.toolbox.registry.ModArmorMaterials;
import xiabis.modpack.toolbox.registry.ModBlockEntities;
import xiabis.modpack.toolbox.registry.ModBlocks;
import xiabis.modpack.toolbox.registry.ModComponents;
import xiabis.modpack.toolbox.registry.ModItems;
import xiabis.modpack.toolbox.registry.ModMenus;
import xiabis.modpack.toolbox.registry.ModPotions;
import xiabis.modpack.toolbox.registry.ModRecipes;

@Mod(XmtMod.MOD_ID)
public class XmtMod {
    public static final String MOD_ID = "xiabis_modpack_toolbox";
    /** Shared logger, used for the occasional diagnostic message. */
    public static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public XmtMod(IEventBus modBus, ModContainer container) {
        ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModComponents.COMPONENTS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModItems.CREATIVE_TABS.register(modBus);
        ModPotions.POTIONS.register(modBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(ModNetwork::register);
        modBus.addListener(XmtMod::registerCapabilities);
        modBus.addListener(XmtMod::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (!ModList.get().isLoaded("curios")) {
                return;
            }
            Item flask = ModItems.MAGIC_POISON_BOTTLE.get();
            if (flask instanceof ICurioItem curio) {
                CuriosApi.registerCurio(flask, curio);
            }
            Item armor = ModItems.OMNI_ARMOR.get();
            if (armor instanceof ICurioItem curio) {
                CuriosApi.registerCurio(armor, curio);
            }
        });
    }

    private static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        if (ModBlockEntities.COMPRESSED_CONDENSER != null) {
            xiabis.modpack.toolbox.blockentity.CompressedCondenserBlockEntity.registerCapabilities(event);
        }
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.OMNI_GENERATOR.get(),
                (generator, side) -> generator.itemHandler(side));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.OMNI_GENERATOR.get(),
                (generator, side) -> generator.fluidHandler(side));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.OMNI_GENERATOR.get(),
                (generator, side) -> generator.energyHandler(side));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.OMNI_BATTERY.get(),
                (battery, side) -> battery.energyHandler(side));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.OMNI_GREENHOUSE.get(),
                (greenhouse, side) -> greenhouse.itemHandler(side));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.OMNI_GREENHOUSE.get(),
                (greenhouse, side) -> greenhouse.fluidHandler(side));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.OMNI_GREENHOUSE.get(),
                (greenhouse, side) -> greenhouse.energyHandler(side));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.OMNI_FLUID_TANK.get(),
                (tank, side) -> tank.fluidHandler());
        if (ModList.get().isLoaded("ae2")) {
            xiabis.modpack.toolbox.blockentity.OmniFluidTankAeStorage.registerCapabilities(event);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
