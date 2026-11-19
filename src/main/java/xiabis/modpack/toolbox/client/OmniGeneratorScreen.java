package xiabis.modpack.toolbox.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import xiabis.modpack.toolbox.menu.OmniGeneratorMenu;
import xiabis.modpack.toolbox.network.GeneratorConfigPayload;
import xiabis.modpack.toolbox.util.GeneratorEnergyType;
import xiabis.modpack.toolbox.util.GeneratorFaceMode;

/** GUI of the omni generator: face routing buttons, fuel slots, tank, energy buffer and card slots. */
public class OmniGeneratorScreen extends AbstractContainerScreen<OmniGeneratorMenu> {
    private static final int PANEL = 0xFF202020;
    private static final int PANEL_LIGHT = 0xFF2C2C2C;
    private static final int BORDER = 0xFF4A4A4A;
    private static final int SLOT_BG = 0xFF121212;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int TEXT_DIM = 0xFF9A9A9A;

    private static final int BUTTON_SIZE = 20;
    private static final int[] BUTTON_X = {8, 30, 52};
    private static final int[] BUTTON_Y = {8, 30, 52};

    private final List<Button> buttons = new ArrayList<>();

    public OmniGeneratorScreen(OmniGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 216;
        this.imageHeight = 200;
        this.inventoryLabelY = 100;
        this.titleLabelY = 78;
        // active output / face up / active input
        buttons.add(new Button(BUTTON_X[0], BUTTON_Y[0], GeneratorConfigPayload.ACTION_ACTIVE_OUTPUT, -1));
        buttons.add(new Button(BUTTON_X[1], BUTTON_Y[0], GeneratorConfigPayload.ACTION_FACE_BASE + 0, 0));
        buttons.add(new Button(BUTTON_X[2], BUTTON_Y[0], GeneratorConfigPayload.ACTION_ACTIVE_INPUT, -1));
        // face left / face front / face right
        buttons.add(new Button(BUTTON_X[0], BUTTON_Y[1], GeneratorConfigPayload.ACTION_FACE_BASE + 1, 1));
        buttons.add(new Button(BUTTON_X[1], BUTTON_Y[1], GeneratorConfigPayload.ACTION_FACE_BASE + 2, 2));
        buttons.add(new Button(BUTTON_X[2], BUTTON_Y[1], GeneratorConfigPayload.ACTION_FACE_BASE + 3, 3));
        // face back / face bottom / energy type
        buttons.add(new Button(BUTTON_X[0], BUTTON_Y[2], GeneratorConfigPayload.ACTION_FACE_BASE + 4, 4));
        buttons.add(new Button(BUTTON_X[1], BUTTON_Y[2], GeneratorConfigPayload.ACTION_FACE_BASE + 5, 5));
        buttons.add(new Button(BUTTON_X[2], BUTTON_Y[2], GeneratorConfigPayload.ACTION_ENERGY_TYPE, -1));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x - 1, y - 1, x + imageWidth + 1, y + imageHeight + 1, BORDER);
        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL);

        for (var slot : menu.slots) {
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, SLOT_BG);
        }

        // Buttons
        for (Button button : buttons) {
            int bx = x + button.x();
            int by = y + button.y();
            graphics.fill(bx, by, bx + BUTTON_SIZE, by + BUTTON_SIZE, buttonColor(button));
            graphics.fill(bx, by, bx + BUTTON_SIZE, by + 1, BORDER);
            graphics.fill(bx, by + BUTTON_SIZE - 1, bx + BUTTON_SIZE, by + BUTTON_SIZE, BORDER);
            graphics.fill(bx, by, bx + 1, by + BUTTON_SIZE, BORDER);
            graphics.fill(bx + BUTTON_SIZE - 1, by, bx + BUTTON_SIZE, by + BUTTON_SIZE, BORDER);
            Component label = buttonLabel(button);
            graphics.drawString(font, label, bx + BUTTON_SIZE / 2 - font.width(label) / 2, by + 6, TEXT, false);
        }

        // Fluid gauge
        int fluidX = x + 150;
        int fluidY = y + 18;
        int fluidHeight = 62;
        graphics.fill(fluidX - 1, fluidY - 1, fluidX + 17, fluidY + fluidHeight + 1, BORDER);
        graphics.fill(fluidX, fluidY, fluidX + 16, fluidY + fluidHeight, 0xFF101010);
        float fluidRatio = (float) menu.fluidAmount() / (float) menu.fluidCapacity();
        int fluidFill = Math.round(fluidHeight * Math.clamp(fluidRatio, 0.0F, 1.0F));
        if (fluidFill > 0) {
            graphics.fill(fluidX, fluidY + fluidHeight - fluidFill, fluidX + 16, fluidY + fluidHeight, 0xFFD2691E);
        }

        // Energy gauge
        int energyX = x + 172;
        int energyY = y + 18;
        int energyHeight = 62;
        graphics.fill(energyX - 1, energyY - 1, energyX + 13, energyY + energyHeight + 1, BORDER);
        graphics.fill(energyX, energyY, energyX + 12, energyY + energyHeight, 0xFF101010);
        float energyRatio = (float) menu.energyStored() / (float) menu.energyCapacity();
        int energyFill = Math.round(energyHeight * Math.clamp(energyRatio, 0.0F, 1.0F));
        if (energyFill > 0) {
            graphics.fill(energyX, energyY + energyHeight - energyFill, energyX + 12, energyY + energyHeight,
                    energyColor(menu.energyType()));
        }

        // Burn progress
        int burnX = x + 82;
        int burnY = y + 82;
        graphics.fill(burnX, burnY, burnX + 54, burnY + 5, SLOT_BG);
        int burnWidth = (int) (54 * (menu.burnTime() / (float) menu.burnTotal()));
        if (burnWidth > 0) {
            graphics.fill(burnX, burnY, burnX + burnWidth, burnY + 5, 0xFFE8A33D);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        GeneratorEnergyType type = menu.energyType();
        long stored = Math.round(type.toUnits(menu.energyStored()));
        long capacity = Math.round(type.toUnits(menu.energyCapacity()));
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.energy",
                stored, capacity, type.displayName()), 82, 92, TEXT_DIM, false);
        graphics.drawString(font, Component.translatable("gui.xiabis_modpack_toolbox.omni_generator.fluid",
                menu.fluidAmount() / 1000, menu.fluidCapacity() / 1000), 82, 78, TEXT_DIM, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_DIM, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        Button hovered = buttonAt(mouseX, mouseY);
        if (hovered != null) {
            graphics.renderTooltip(font, buttonTooltip(hovered), mouseX, mouseY);
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

    private Button buttonAt(double mouseX, double mouseY) {
        for (Button button : buttons) {
            int bx = leftPos + button.x();
            int by = topPos + button.y();
            if (mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE) {
                return button;
            }
        }
        return null;
    }

    private int buttonColor(Button button) {
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_OUTPUT) {
            return menu.activeOutput() ? 0xFF2E7D32 : 0xFF3A2A2A;
        }
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_INPUT) {
            return menu.activeInput() ? 0xFF2E7D32 : 0xFF3A2A2A;
        }
        return PANEL_LIGHT;
    }

    private Component buttonLabel(Button button) {
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_OUTPUT) {
            return Component.literal("OUT");
        }
        if (button.action() == GeneratorConfigPayload.ACTION_ACTIVE_INPUT) {
            return Component.literal("IN");
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

    private record Button(int x, int y, int action, int guiFace) {
    }
}
