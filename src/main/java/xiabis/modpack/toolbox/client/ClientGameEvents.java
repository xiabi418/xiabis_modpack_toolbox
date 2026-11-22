package xiabis.modpack.toolbox.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.item.OmniToolItem;

@EventBusSubscriber(modid = XmtMod.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        HighlightCache.tick(minecraft);
        while (ClientModEvents.OPEN_ARMOR_MENU.consumeClick()) {
            if (xiabis.modpack.toolbox.util.ArmorSupport.hasArmor(minecraft.player)) {
                net.neoforged.neoforge.network.PacketDistributor
                        .sendToServer(new xiabis.modpack.toolbox.network.OpenArmorMenuPayload());
            }
        }
        while (ClientModEvents.OPEN_MENU.consumeClick()) {
            ItemStack stack = minecraft.player.getMainHandItem();
            if (stack.getItem() instanceof OmniToolItem) {
                minecraft.setScreen(new OmniToolScreen(stack));
            }
        }
    }

    private ClientGameEvents() {
    }
}
