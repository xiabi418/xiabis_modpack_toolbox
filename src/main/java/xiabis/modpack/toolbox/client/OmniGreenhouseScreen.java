package xiabis.modpack.toolbox.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.menu.OmniGreenhouseMenu;
import xiabis.modpack.toolbox.network.GreenhouseConfigPayload;
import xiabis.modpack.toolbox.util.GreenhouseFaceMode;

/**
 * GUI of the omni greenhouse. The generated sheet supplies the panel plus the 3x3 config button
 * cluster on its right; all coordinates below are in sheet space.
 */
public class OmniGreenhouseScreen extends AbstractContainerScreen<OmniGreenhouseMenu> {
    private static final ResourceLocation BACKGROUND = XmtMod.id("textures/gui/omni_greenhouse.png");
    private static final int TEXT = 0xFF3A3A3A;

    private static final int BUTTON_SIZE_W = 16;
    private static final int BUTTON_SIZE_H = 14;
    private static final int BUTTON_X = 185;
    private static final int BUTTON_Y = 30;
    private static final int BUTTON_PITCH = 18;

    private static final int WATER_X = 10;
    private static final int ENERGY_X = 33;
    private static final int GAUGE_Y = 30;
    private static final int GAUGE_WIDTH = 16;
    private static final int GAUGE_HEIGHT = 50;

    private static final int PROGRESS_X = 64;
    private static final int PROGRESS_Y = 56;
    private static final int PROGRESS_WIDTH = 34;
    private static final int PROGRESS_HEIGHT = 3;

    private final List<Button> buttons = new ArrayList<>();

    public OmniGreenhouseScreen(OmniGreenhouseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 245;
        this.imageHeight = 186;
        this.titleLabelY = 6;
        this.inventoryLabelY = 91;

        // Row 1: active output / face up / active input.
        buttons.add(new Button(0, 0, GreenhouseConfigPayload.ACTION_ACTIVE_OUTPUT, -1));
        buttons.add(new Button(1, 0, GreenhouseConfigPayload.ACTION_FACE_BASE, 0));
        buttons.add(new Button(2, 0, GreenhouseConfigPayload.ACTION_ACTIVE_INPUT, -1));
        // Row 2: face left / face front / face right.
        buttons.add(new Button(0, 1, GreenhouseConfigPayload.ACTION_FACE_BASE + 1, 1));
        buttons.add(new Button(1, 1, GreenhouseConfigPayload.ACTION_FACE_BASE + 2, 2));
        buttons.add(new Button(2, 1, GreenhouseConfigPayload.ACTION_FACE_BASE + 3, 3));
        // Row 3: face back / face down / cycle time info.
        buttons.add(new Button(0, 2, GreenhouseConfigPayload.ACTION_FACE_BASE + 4, 4));
        buttons.add(new Button(1, 2, GreenhouseConfigPayload.ACTION_FACE_BASE + 5, 5));
        buttons.add(new Button(2, 2, ACTION_INFO, -1));
    }

    /** Pseudo action for the bottom right button: it only shows the current cycle in its tooltip. */
    private static final int ACTION_INFO = -2;

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        float waterRatio = menu.fluidCapacity() <= 0 ? 0.0F
                : (float) menu.fluidAmount() / (float) menu.fluidCapacity();
        int waterFill = Math.round(GAUGE_HEIGHT * Math.clamp(waterRatio, 0.0F, 1.0F));
        if (waterFill > 0) {
            graphics.fill(leftPos + WATER_X, topPos + GAUGE_Y + GAUGE_HEIGHT - waterFill,
                    leftPos + WATER_X + GAUGE_WIDTH, topPos + GAUGE_Y + GAUGE_HEIGHT, 0xFF3D7BE8);
        }

        float energyRatio = menu.energyCapacity() <= 0 ? 0.0F
                : (float) menu.energyStored() / (float) menu.energyCapacity();
        int energyFill = Math.round(GAUGE_HEIGHT * Math.clamp(energyRatio, 0.0F, 1.0F));
        if (energyFill > 0) {
            graphics.fill(leftPos + ENERGY_X, topPos + GAUGE_Y + GAUGE_HEIGHT - energyFill,
                    leftPos + ENERGY_X + GAUGE_WIDTH, topPos + GAUGE_Y + GAUGE_HEIGHT, 0xFF39D353);
        }

        float cycleRatio = Math.clamp(menu.progress() / (float) menu.period(), 0.0F, 1.0F);
        int cycleFill = Math.round(PROGRESS_WIDTH * cycleRatio);
        if (cycleFill > 0) {
            graphics.fill(leftPos + PROGRESS_X, topPos + PROGRESS_Y, leftPos + PROGRESS_X + cycleFill,
                    topPos + PROGRESS_Y + PROGRESS_HEIGHT, 0xFF7ED957);
        }

        for (Button button : buttons) {
            int color = buttonColor(button);
            if (color == 0) {
                continue;
            }
            int bx = leftPos + buttonX(button);
            int by = topPos + buttonY(button);
            graphics.fill(bx, by, bx + BUTTON_SIZE_W, by + BUTTON_SIZE_H, color);
        }
        for (Button button : buttons) {
            Component label = buttonLabel(button);
            int bx = leftPos + buttonX(button);
            int by = topPos + buttonY(button);
            graphics.drawString(font, label, bx + BUTTON_SIZE_W / 2 - font.width(label) / 2, by + 3,
                    0xFFFFFFFF, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        Button hovered = buttonAt(mouseX, mouseY);
        if (hovered != null) {
            graphics.renderTooltip(font, buttonTooltip(hovered), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + WATER_X, topPos + GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT)) {
            graphics.renderTooltip(font,
                    Component.translatable("gui.xiabis_modpack_toolbox.omni_greenhouse.water",
                            menu.fluidAmount() / 1000, menu.fluidCapacity() / 1000),
                    mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + ENERGY_X, topPos + GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT)) {
            graphics.renderTooltip(font,
                    Component.translatable("gui.xiabis_modpack_toolbox.omni_greenhouse.energy",
                            menu.energyStored(), menu.energyCapacity()),
                    mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + PROGRESS_X, topPos + PROGRESS_Y,
                PROGRESS_WIDTH, PROGRESS_HEIGHT + 2)) {
            graphics.renderTooltip(font,
                    Component.translatable("gui.xiabis_modpack_toolbox.omni_greenhouse.cycle",
                            String.format("%.2f", menu.period() / 20.0F)),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Button hovered = buttonAt(mouseX, mouseY);
        if (hovered != null && (button == 0 || button == 1) && hovered.action() != ACTION_INFO) {
            int direction = button == 1 ? -1 : 1;
            PacketDistributor.sendToServer(new GreenhouseConfigPayload(hovered.action(), direction));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static int buttonX(Button button) {
        return BUTTON_X + button.column() * BUTTON_PITCH;
    }

    private static int buttonY(Button button) {
        return BUTTON_Y + button.row() * BUTTON_PITCH;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private Button buttonAt(double mouseX, double mouseY) {
        for (Button button : buttons) {
            int bx = leftPos + buttonX(button);
            int by = topPos + buttonY(button);
            if (inside(mouseX, mouseY, bx, by, BUTTON_SIZE_W, BUTTON_SIZE_H)) {
                return button;
            }
        }
        return null;
    }

    private int buttonColor(Button button) {
        if (button.action() == GreenhouseConfigPayload.ACTION_ACTIVE_OUTPUT) {
            return menu.activeOutput() ? 0x802E7D32 : 0x803A2A2A;
        }
        if (button.action() == GreenhouseConfigPayload.ACTION_ACTIVE_INPUT) {
            return menu.activeInput() ? 0x802E7D32 : 0x803A2A2A;
        }
        if (button.action() >= GreenhouseConfigPayload.ACTION_FACE_BASE) {
            return switch (menu.faceMode(button.guiFace())) {
                case INPUT_FLUID -> 0x803B6EC8;
                case INPUT_ENERGY -> 0x80C8B03B;
                case OUTPUT_ITEMS -> 0x80C83B3B;
                case BOTH -> 0x8058A85A;
            };
        }
        return 0;
    }

    private Component buttonLabel(Button button) {
        if (button.action() == GreenhouseConfigPayload.ACTION_ACTIVE_OUTPUT) {
            return Component.literal(menu.activeOutput() ? "OUT" : "out");
        }
        if (button.action() == GreenhouseConfigPayload.ACTION_ACTIVE_INPUT) {
            return Component.literal(menu.activeInput() ? "IN" : "in");
        }
        if (button.action() == ACTION_INFO) {
            return Component.literal(menu.period() + "t");
        }
        Direction direction = menu.guiDirection(button.guiFace());
        return Component.literal(GreenhouseConfigPayload.faceLetter(direction));
    }

    private Component buttonTooltip(Button button) {
        if (button.action() == GreenhouseConfigPayload.ACTION_ACTIVE_OUTPUT) {
            return Component.translatable("gui.xiabis_modpack_toolbox.omni_greenhouse.active_output",
                    Component.translatable(menu.activeOutput()
                            ? "gui.xiabis_modpack_toolbox.on" : "gui.xiabis_modpack_toolbox.off"));
        }
        if (button.action() == GreenhouseConfigPayload.ACTION_ACTIVE_INPUT) {
            return Component.translatable("gui.xiabis_modpack_toolbox.omni_greenhouse.active_input",
                    Component.translatable(menu.activeInput()
                            ? "gui.xiabis_modpack_toolbox.on" : "gui.xiabis_modpack_toolbox.off"));
        }
        if (button.action() == ACTION_INFO) {
            return Component.translatable("gui.xiabis_modpack_toolbox.omni_greenhouse.cycle",
                    String.format("%.2f", menu.period() / 20.0F));
        }
        GreenhouseFaceMode mode = menu.faceMode(button.guiFace());
        return Component.translatable("gui.xiabis_modpack_toolbox.omni_greenhouse.face",
                menu.guiDirection(button.guiFace()).getName(), Component.translatable(mode.translationKey()));
    }

    private record Button(int column, int row, int action, int guiFace) {
    }
}
