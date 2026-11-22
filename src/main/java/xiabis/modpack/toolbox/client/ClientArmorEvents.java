package xiabis.modpack.toolbox.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.util.ArmorSupport;

/**
 * Removes every kind of fog for a player wearing the omni armor.
 */
@EventBusSubscriber(modid = XmtMod.MOD_ID, value = Dist.CLIENT)
public final class ClientArmorEvents {
    private ClientArmorEvents() {
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && ArmorSupport.worn(minecraft.player)) {
            event.setCanceled(true);
        }
    }
}
