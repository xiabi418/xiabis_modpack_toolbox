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
import xiabis.modpack.toolbox.menu.OmniGeneratorMenu;
import xiabis.modpack.toolbox.network.GeneratorConfigPayload;
import xiabis.modpack.toolbox.util.GeneratorEnergyType;
import xiabis.modpack.toolbox.util.GeneratorFaceMode;

/**
 * GUI of the omni generator. The hand drawn sheet supplies the whole panel plus the 3x3 config
 * button cluster on its right; all coordinates below are in sheet space.
 */
public class OmniGeneratorScreen extends AbstractContainerScreen<OmniGeneratorMenu> {
    private static final ResourceLocation BACKGROUND = XmtMod.id("textures/gui/omni_generator.png");
    /** The panel is drawn below a 4 pixel margin in the sheet. */
    private static final int TEXTURE_TOP = 0;
    private static final int TEXT = 0xFF3A3A3A;
    private static final int TEXT_DIM = 0xFF5A5A5A;

    private static final int BUTTON_SIZE_W = 18;
    private static final int BUTTON_SIZE_H = 16;
    private static final int BUTTON_X = 184;
    private static final int BUTTON_Y = 29;
    private static final int BUTTON_PITCH = 18;

    private static final int LAVA_X = 10;
    private static final int GAUGE_Y = 30;
    private static final int GAUGE_WIDTH = 16;
    private static final int GAUGE_HEIGHT = 50;
    private static final int ENERGY_X = 150;
    private static final int BURN_X = 98;
    private static final int BURN_Y = 46;
    private static final int BURN_WIDTH = 41;
    private static final int BURN_HEIGHT = 3;

    private final List<Button> buttons = new ArrayList<>();

    public OmniGeneratorScreen(OmniGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 245;
        this.imageHeight = 186;
        this.titleLabelY = 6;
        this.inventoryLabelY = 91;
        // Row 1: active output / face up / active input.
        buttons.add(new Button(0, 0, GeneratorConfigPayload.ACTION_ACTIVE_OUTPUT, -1));
        buttons.add(new Button(1, 0, GeneratorConfigPayload.ACTION_FACE_BASE, 0));
        buttons.add(new Button(2, 0, GeneratorConfigPayload.ACTION_ACTIVE_INPUT, -1));
        // Row 2: face left / face front / face right.
        buttons.add(new Button(0, 1, GeneratorConfigPayload.ACTION_FACE_BASE + 1, 1));
        buttons.add(new Button(1, 1, GeneratorConfigPayload.ACTION_FACE_BASE + 2, 2));
        buttons.add(new Button(2, 1, GeneratorConfigPayload.ACTION_FACE_BASE + 3, 3));
        // Row 3: face back / face down / energy type.
        buttons.add(new Button(0, 2, GeneratorConfigPayload.ACTION_FACE_BASE + 4, 4));
        buttons.add(new Button(1, 2, GeneratorConfigPayload.ACTION_FACE_BASE + 5, 5));
        buttons.add(new Button(2, 2, GeneratorConfigPayload.ACTION_ENERGY_TYPE, -1));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, TEXTURE_TOP, imageWidth, imageHeight, 256, 256);

        // Lava tank, filled from the bottom of the left gauge.
        float fluidRatio = menu.fluidCapacity() <= 0 ? 0.0F
                : (float) menu.fluidAmount() / (float) menu.fluidCapacity();
        int fluidFill = Math.round(GAUGE_HEIGHT * Math.clamp(fluidRatio, 0.0F, 1.0F));
        if (fluidFill > 0) {
            graphics.fill(leftPos + LAVA_X, topPos + GAUGE_Y + GAUGE_HEIGHT - fluidFill,
                    leftPos + LAVA_X + GAUGE_WIDTH, topPos + GAUGE_Y + GAUGE_HEIGHT, 0xFFD2691E);
        }

        // Energy buffer in the right gauge.
        float energyRatio = menu.energyCapacity() <= 0 ? 0.0F
                : (float) menu.energyStored() / (float) menu.energyCapacity();
        int energyFill = Math.round(GAUGE_HEIGHT * Math.clamp(energyRatio, 0.0F, 1.0F));
        if (energyFill > 0) {
            graphics.fill(leftPos + ENERGY_X, topPos + GAUGE_Y + GAUGE_HEIGHT - energyFill,
                    leftPos + ENERGY_X + GAUGE_WIDTH, topPos + GAUGE_Y + GAUGE_HEIGHT,
                    energyColor(menu.energyType()));
        }

        // Fuel burn progress inside the thin bar.
        if (menu.burnTotal() > 0) {
            int burnWidth = Math.round(BURN_WIDTH * Math.clamp(menu.burnTime() / (float) menu.burnTotal(),
                    0.0F, 1.0F));
            if (burnWidth > 0) {
                graphics.fill(leftPos + BURN_X, topPos + BURN_Y, leftPos + BURN_X + burnWidth,
                        topPos + BURN_Y + BURN_HEIGHT, 0xFFE8A33D);
            }
        }

        // Face / toggle buttons: highlight what is active.
        for (Button button : buttons) {
            int color = buttonColor(button);
            if (color == 0) {
                continue;
            }
            int bx = leftPos + buttonX(button);
            int by = topPos + buttonY(button);
            graphics.fill(bx, by, bx + BUTTON_SIZE_W, by + BUTTON_SIZE_H, color);
        }

        // Short labels inside the buttons.
        for (Button button : buttons) {
            Component label = buttonLabel(button);
            int bx = leftPos + buttonX(button);
            int by = topPos + buttonY(button);
            graphics.drawString(font, label, bx + BUTTON_SIZE_W / 2 - font.width(label) / 2, by + 4,
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
        } else if (inside(mouseX, mouseY, leftPos + LAVA_X, topPos + GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT)) {
            graphics.renderTooltip(font,
                    Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.fluid",
                            menu.fluidAmount() / 1000, menu.fluidCapacity() / 1000),
                    mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + ENERGY_X, topPos + GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT)) {
            GeneratorEnergyType type = menu.energyType();
            graphics.renderTooltip(font,
                    Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.energy",
                            Math.round(type.toUnits(menu.energyStored())),
                            Math.round(type.toUnits(menu.energyCapacity())), type.displayName()),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Button hovered = buttonAt(mouseX, mouseY);
        if (hovered != null && (button == 0 || button == 1)) {
            int direction = button == 1 ? -1 : 1;
            PacketDistributor.sendToServer(new GeneratorConfigPayload(hovered.action(), direction));
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

    /** 0 means "draw nothing special"; the sheet already has the button faces. */
    private int buttonColor(Button button) {
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_OUTPUT) {
            return menu.activeOutput() ? 0x802E7D32 : 0x803A2A2A;
        }
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_INPUT) {
            return menu.activeInput() ? 0x802E7D32 : 0x803A2A2A;
        }
        if (button.action() >= GeneratorConfigPayload.ACTION_FACE_BASE) {
            return switch (menu.faceMode(button.guiFace())) {
                case INPUT_FLUID -> 0x803B6EC8;
                case INPUT_ITEM -> 0x80C88A3B;
                case OUTPUT_ENERGY -> 0x80C83B3B;
                case BOTH -> 0x8058A85A;
            };
        }
        return 0;
    }

    private Component buttonLabel(Button button) {
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_OUTPUT) {
            return Component.literal(menu.activeOutput() ? "OUT" : "out");
        }
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_INPUT) {
            return Component.literal(menu.activeInput() ? "IN" : "in");
        }
        if (button.action() == GeneratorConfigPayload.ACTION_ENERGY_TYPE) {
            return menu.energyType().displayName();
        }
        Direction direction = menu.guiDirection(button.guiFace());
        return Component.literal(GeneratorConfigPayload.faceLetter(direction));
    }

    private Component buttonTooltip(Button button) {
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_OUTPUT) {
            return Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.active_output",
                    Component.translatable(menu.activeOutput()
                            ? "gui.xiabis_modpack_toolbox.on" : "gui.xiabis_modpack_toolbox.off"));
        }
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_INPUT) {
            return Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.active_input",
                    Component.translatable(menu.activeInput()
                            ? "gui.xiabis_modpack_toolbox.on" : "gui.xiabis_modpack_toolbox.off"));
        }
        if (button.action() == GeneratorConfigPayload.ACTION_ENERGY_TYPE) {
            return Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.energy_type",
                    menu.energyType().displayName());
        }
        GeneratorFaceMode mode = menu.faceMode(button.guiFace());
        return Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.face",
                menu.guiDirection(button.guiFace()).getName(), mode.displayName());
    }

    private static int energyColor(GeneratorEnergyType type) {
        return switch (type) {
            case FE -> 0xFF39D353;
            case EU -> 0xFFE8C13D;
            case AE -> 0xFF4A9EE8;
        };
    }

    private record Button(int column, int row, int action, int guiFace) {
    }
}
