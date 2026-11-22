package xiabis.modpack.toolbox.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xiabis.modpack.toolbox.menu.OmniArmorMenu;

/**
 * The marker editor of the omni armor: a 4x9 grid that accepts block items (also as JEI ghosts).
 */
public class OmniArmorScreen extends AbstractContainerScreen<OmniArmorMenu> {
    private static final int PANEL_COLOR = 0xF0101018;
    private static final int SLOT_COLOR = 0xFF2A2A38;
    private static final int SLOT_BORDER = 0xFF4A4A60;

    public OmniArmorScreen(OmniArmorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 196;
        this.titleLabelY = 6;
        this.inventoryLabelY = 92;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        Component hint = Component.translatable("gui.xiabis_modpack_toolbox.omni_armor.hint");
        graphics.drawString(font, hint, leftPos + 8, topPos + imageHeight - 10, 0xFF8899AA, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL_COLOR);
        for (int index = 0; index < OmniArmorMenu.MARKER_SLOTS; index++) {
            int x = leftPos + 8 + (index % OmniArmorMenu.COLUMNS) * 18 - 1;
            int y = topPos + 20 + (index / OmniArmorMenu.COLUMNS) * 18 - 1;
            graphics.fill(x, y, x + 18, y + 18, SLOT_BORDER);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_COLOR);
        }
    }
}
