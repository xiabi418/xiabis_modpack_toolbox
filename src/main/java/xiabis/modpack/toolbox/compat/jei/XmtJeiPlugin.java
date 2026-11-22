package xiabis.modpack.toolbox.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.client.OmniArmorScreen;

/**
 * Registers the JEI ghost handlers. Only loaded when JEI is installed.
 */
@JeiPlugin
public class XmtJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return XmtMod.id("jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(OmniArmorScreen.class, new ArmorGhostHandler());
    }
}
