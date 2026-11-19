package xiabis.modpack.toolbox.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xiabis.modpack.toolbox.blockentity.OmniGeneratorBlockEntity;
import xiabis.modpack.toolbox.item.OmniToolItem;
import xiabis.modpack.toolbox.menu.OmniGeneratorMenu;
import xiabis.modpack.toolbox.registry.ModComponents;
import xiabis.modpack.toolbox.util.GeneratorFaceMode;

public final class ModNetwork {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(SetToolModePayload.TYPE, SetToolModePayload.CODEC, ModNetwork::handle);
        registrar.playToServer(GeneratorConfigPayload.TYPE, GeneratorConfigPayload.CODEC,
                ModNetwork::handleGeneratorConfig);
    }

    private static void handle(SetToolModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof OmniToolItem) {
                    stack.set(ModComponents.TOOL_MODE, payload.mode());
                    stack.set(ModComponents.SILK_MODE, payload.silk());
                    if (player.level() instanceof ServerLevel serverLevel) {
                        OmniToolItem.applyState(stack, serverLevel.registryAccess());
                    }
                    player.getInventory().setChanged();
                    return;
                }
            }
        });
    }

    private static void handleGeneratorConfig(GeneratorConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof OmniGeneratorMenu menu)) {
                return;
            }
            OmniGeneratorBlockEntity generator = menu.generator();
            if (generator == null) {
                return;
            }
            int direction = payload.value() < 0 ? -1 : 1;
            switch (payload.action()) {
                case GeneratorConfigPayload.ACTION_ACTIVE_OUTPUT ->
                        generator.setActiveOutput(!generator.activeOutput());
                case GeneratorConfigPayload.ACTION_ACTIVE_INPUT ->
                        generator.setActiveInput(!generator.activeInput());
                case GeneratorConfigPayload.ACTION_ENERGY_TYPE ->
                        generator.setEnergyType(generator.energyType().next(direction));
                default -> {
                    int guiFace = payload.action() - GeneratorConfigPayload.ACTION_FACE_BASE;
                    if (guiFace < 0 || guiFace > 5) {
                        return;
                    }
                    java.util.Objects.requireNonNull(menu.guiDirection(guiFace));
                    GeneratorFaceMode mode = generator.faceMode(menu.guiDirection(guiFace));
                    generator.setFaceMode(menu.guiDirection(guiFace), mode.next(direction));
                }
            }
            generator.setChanged();
            menu.broadcastChanges();
        });
    }

    private ModNetwork() {
    }
}
