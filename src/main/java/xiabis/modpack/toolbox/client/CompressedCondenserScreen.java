package xiabis.modpack.toolbox.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.menu.CompressedCondenserMenu;
import xiabis.modpack.toolbox.network.CondenserConfigPayload;

import java.io.IOException;
import java.io.UncheckedIOException;

final class CompressedCondenserScreen extends AEBaseScreen<CompressedCondenserMenu> {
    private static final String TEXT = "gui.xiabis_modpack_toolbox.compressed_condenser.";

    CompressedCondenserScreen(CompressedCondenserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, loadStyle());
        var output = widgets.addButton("output", Component.translatable(TEXT + "output"),
                () -> send(CondenserConfigPayload.TOGGLE_OUTPUT));
        var mode = widgets.addButton("mode", Component.translatable(TEXT + "mode_button"),
                () -> send(CondenserConfigPayload.TOGGLE_MODE));
        output.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable(TEXT + "output")));
        mode.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable(TEXT + "mode_button")));
    }

    private static ScreenStyle loadStyle() {
        var id = XmtMod.id("screens/compressed_condenser.json");
        try (var reader = Minecraft.getInstance().getResourceManager().openAsReader(id)) {
            ScreenStyle screenStyle = ScreenStyle.GSON.fromJson(reader, ScreenStyle.class);
            screenStyle.validate();
            return screenStyle;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read screen style " + id, e);
        }
    }

    private static void send(int action) {
        PacketDistributor.sendToServer(new CondenserConfigPayload(action));
    }

    @Override protected boolean shouldAddToolbar() { return false; }

    @Override public void drawBG(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
        super.drawBG(graphics, x, y, mouseX, mouseY, partialTick);
        for (Slot slot : menu.slots) {
            int sx = x + slot.x - 1, sy = y + slot.y - 1;
            graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
            graphics.fill(sx + 1, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFFBDBDBD);
        }
    }

    @Override public void drawFG(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        super.drawFG(graphics, x, y, mouseX, mouseY);
        var mode = Component.translatable(TEXT + (menu.singularityMode() ? "singularity" : "matter"));
        var output = Component.translatable(menu.activeOutput() ? "options.on" : "options.off");
        int textColor = 0x413F54;
        graphics.drawString(font, Component.translatable(TEXT + "mode", mode), 8, 70, textColor, false);
        graphics.drawString(font, Component.translatable(TEXT + "output_state", output), 8, 83, textColor, false);
        graphics.drawString(font, Component.translatable(TEXT + "value", menu.displayedValue()), 114, 82, textColor, false);
        var rate = Component.translatable(TEXT + "rate", menu.rate());
        graphics.drawString(font, rate, 312 - font.width(rate), 100, textColor, false);
    }
}
