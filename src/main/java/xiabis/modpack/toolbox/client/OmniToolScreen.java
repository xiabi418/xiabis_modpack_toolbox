package xiabis.modpack.toolbox.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import xiabis.modpack.toolbox.item.OmniToolItem;
import xiabis.modpack.toolbox.item.ToolMode;
import xiabis.modpack.toolbox.network.SetToolModePayload;

/** Mode / silk-touch selection screen opened with the configured key (default V). */
public class OmniToolScreen extends Screen {
    private static final List<ToolMode> MODES = List.of(
            ToolMode.ALL, ToolMode.PICKAXE, ToolMode.SHOVEL,
            ToolMode.AXE, ToolMode.HOE, ToolMode.SHEARS,
            ToolMode.SHIELD, ToolMode.WRENCH, ToolMode.STAFF);

    private final ItemStack stack;
    private ToolMode mode;
    private boolean silk;
    private Button silkButton;

    public OmniToolScreen(ItemStack stack) {
        super(Component.translatable("screen.xiabis_modpack_toolbox.omni_tool"));
        this.stack = stack;
        this.mode = OmniToolItem.mode(stack);
        this.silk = OmniToolItem.silkMode(stack);
    }

    @Override
    protected void init() {
        int gridWidth = 3 * 78;
        int x0 = (this.width - gridWidth) / 2;
        int y0 = this.height / 2 - 60;
        for (int index = 0; index < MODES.size(); index++) {
            ToolMode candidate = MODES.get(index);
            int column = index % 3;
            int row = index / 3;
            Button button = Button.builder(label(candidate), b -> select(candidate))
                    .bounds(x0 + column * 78, y0 + row * 24, 74, 20)
                    .build();
            this.addRenderableWidget(button);
        }
        this.silkButton = this.addRenderableWidget(Button.builder(silkLabel(), b -> {
            this.silk = !this.silk;
            b.setMessage(silkLabel());
            send();
        }).bounds(x0, y0 + 84, 148, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(x0 + 152, y0 + 84, 82, 20)
                .build());
    }

    private Component label(ToolMode candidate) {
        String prefix = candidate == this.mode ? "> " : "";
        return Component.literal(prefix).append(candidate.displayName());
    }

    private Component silkLabel() {
        return Component.translatable("screen.xiabis_modpack_toolbox.silk")
                .append(": ")
                .append(Component.translatable(this.silk
                        ? "screen.xiabis_modpack_toolbox.on"
                        : "screen.xiabis_modpack_toolbox.off"));
    }

    private void select(ToolMode candidate) {
        this.mode = candidate;
        send();
        this.rebuildWidgets();
    }

    private void send() {
        PacketDistributor.sendToServer(new SetToolModePayload(this.mode.ordinal(), this.silk));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 84, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.xiabis_modpack_toolbox.current", this.mode.displayName()),
                this.width / 2, this.height / 2 + 40, 0xA0A0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
