package xiabis.modpack.toolbox.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import xiabis.modpack.toolbox.menu.FloorBuilderMenu;

public final class FloorBuilderScreen extends AbstractContainerScreen<FloorBuilderMenu> {
    private static final String KEY = "menu.xiabis_modpack_toolbox.floor_builder.";
    private static final String[] LABELS = {"border", "fill", "glow", "size", "replace", "build"};
    private final Button[] buttons = new Button[LABELS.length];

    public FloorBuilderScreen(FloorBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 220;
        imageHeight = 169;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < buttons.length; i++) {
            final int id = i;
            buttons[i] = addRenderableWidget(Button.builder(label(i), button -> {
                if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }).bounds(leftPos + 10, topPos + 22 + i * 23, imageWidth - 20, 20).build());
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (int i = 0; i < buttons.length; i++) buttons[i].setMessage(label(i));
    }

    private Component label(int id) {
        String key = KEY + LABELS[id];
        if (id == FloorBuilderMenu.BUILD) return Component.translatable(key);
        int value = menu.option(id);
        return switch (id) {
            case FloorBuilderMenu.BORDER, FloorBuilderMenu.FILL ->
                    Component.translatable(key, Component.translatable("color.minecraft." + DyeColor.byId(value).getName()));
            case FloorBuilderMenu.SIZE -> Component.translatable(key, value + 1, 2 * value + 1, 2 * value + 1);
            default -> Component.translatable(key, Component.translatable(value != 0 ? "options.on" : "options.off"));
        };
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF202226);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 8, 0xFFFFFF, false);
    }
}
