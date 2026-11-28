package xiabis.modpack.toolbox.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xiabis.modpack.toolbox.blockentity.OmniDiggerBlockEntity;
import xiabis.modpack.toolbox.menu.OmniDiggerMenu;

public final class OmniDiggerScreen extends AbstractContainerScreen<OmniDiggerMenu> {
    private static final String KEY = "menu.xiabis_modpack_toolbox.omni_digger.";
    private static final String[] STATUS = {"idle", "working", "output", "power", "complete"};
    private Button mode;
    private Button run;

    public OmniDiggerScreen(OmniDiggerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 136;
    }

    @Override protected void init() {
        super.init();
        mode = addRenderableWidget(Button.builder(modeLabel(), button -> click(0))
                .bounds(leftPos + 10, topPos + 103, 128, 22).build());
        run = addRenderableWidget(Button.builder(runLabel(), button -> click(1))
                .bounds(leftPos + 144, topPos + 103, 76, 22).build());
    }

    @Override protected void containerTick() {
        mode.setMessage(modeLabel());
        run.setMessage(runLabel());
    }

    private void click(int id) {
        if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private Component modeLabel() { return Component.translatable(KEY + (menu.silk() ? "silk" : "fortune")); }
    private Component runLabel() { return Component.translatable(KEY + (menu.enabled() ? "stop" : "start")); }

    @Override protected void renderBg(GuiGraphics gui, float tick, int mouseX, int mouseY) {
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xff202226);
        gui.fill(leftPos + 10, topPos + 37, leftPos + 220, topPos + 43, 0xff3a4048);
        gui.fill(leftPos + 10, topPos + 37,
                leftPos + 10 + 210 * menu.energy() / OmniDiggerBlockEntity.CAPACITY, topPos + 43, 0xff59bb88);
    }

    @Override protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 10, 9, 0xffffff, false);
        gui.drawString(font, Component.translatable(KEY + "energy", menu.energy(), OmniDiggerBlockEntity.CAPACITY), 10, 24, 0xffffff, false);
        gui.drawString(font, Component.translatable(KEY + STATUS[Math.clamp(menu.status(), 0, STATUS.length - 1)]), 10, 51, 0xffffff, false);
        gui.drawString(font, Component.translatable(KEY + "depth", menu.currentY(), menu.bottomY()), 10, 66, 0xffffff, false);
        gui.drawString(font, Component.translatable(KEY + "progress", menu.progress()), 10, 81, 0xffffff, false);
    }
}
