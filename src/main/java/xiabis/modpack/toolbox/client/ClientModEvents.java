package xiabis.modpack.toolbox.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.item.OmniWeaponItem;
import xiabis.modpack.toolbox.registry.ModItems;
import xiabis.modpack.toolbox.registry.ModMenus;

@EventBusSubscriber(modid = XmtMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    public static final String CATEGORY = "key.categories.xiabis_modpack_toolbox";
    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.xiabis.modpack_toolbox.open_menu",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_V,
            CATEGORY);

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.OMNI_GENERATOR.get(), OmniGeneratorScreen::new);
    }

    /** The weapon is not a vanilla bow, so the draw predicates have to be registered by hand. */
    @SubscribeEvent
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.OMNI_WEAPON.get(),
                    ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null || entity.getUseItem() != stack) {
                            return 0.0F;
                        }
                        int used = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
                        int full = OmniWeaponItem.fullChargeTicks(stack, level);
                        return Math.min(1.0F, (float) used / (float) full);
                    });
            ItemProperties.register(ModItems.OMNI_WEAPON.get(),
                    ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) ->
                            entity != null && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    private ClientModEvents() {
    }
}
