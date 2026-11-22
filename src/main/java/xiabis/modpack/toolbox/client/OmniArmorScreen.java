package xiabis.modpack.toolbox.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xiabis.modpack.toolbox.menu.OmniArmorMenu;

/**
 * The marker editor of the omni armor: a 4x9 grid that accepts block items (also as JEI ghosts).
 * The background is the hand drawn sheet; its panel starts at y=4 and is 176x182 pixels.
 */
public class OmniArmorScreen extends AbstractContainerScreen<OmniArmorMenu> {
    private static final net.minecraft.resources.ResourceLocation BACKGROUND =
            xiabis.modpack.toolbox.XmtMod.id("textures/gui/omni_armor.png");
    private static final int TEXTURE_TOP = 0;
    private static final int TEXT = 0xFF3A3A3A;
    private static final int TEXT_DIM = 0xFF5A5A5A;

    public OmniArmorScreen(OmniArmorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.titleLabelY = 6;
        this.inventoryLabelY = 91;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        Component hint = Component.translatable("gui.xiabis_modpack_toolbox.omni_armor.hint");
        graphics.drawString(font, hint, leftPos + 8, topPos + imageHeight + 4, TEXT_DIM, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, TEXTURE_TOP, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
    }
}
