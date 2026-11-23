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
        registrar.playToServer(GreenhouseConfigPayload.TYPE, GreenhouseConfigPayload.CODEC,
                ModNetwork::handleGreenhouseConfig);
        registrar.playToServer(GeneratorConfigPayload.TYPE, GeneratorConfigPayload.CODEC,
                ModNetwork::handleGeneratorConfig);
        registrar.playToServer(OpenArmorMenuPayload.TYPE, OpenArmorMenuPayload.CODEC,
                ModNetwork::handleOpenArmorMenu);
        registrar.playToServer(SetArmorMarkerPayload.TYPE, SetArmorMarkerPayload.CODEC,
                ModNetwork::handleSetArmorMarker);
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

    private static void handleGreenhouseConfig(GreenhouseConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof xiabis.modpack.toolbox.menu.OmniGreenhouseMenu menu)) {
                return;
            }
            xiabis.modpack.toolbox.blockentity.OmniGreenhouseBlockEntity greenhouse = menu.greenhouse();
            if (greenhouse == null) {
                return;
            }
            int direction = payload.value() < 0 ? -1 : 1;
            switch (payload.action()) {
                case GreenhouseConfigPayload.ACTION_ACTIVE_OUTPUT ->
                        greenhouse.setActiveOutput(!greenhouse.activeOutput());
                case GreenhouseConfigPayload.ACTION_ACTIVE_INPUT ->
                        greenhouse.setActiveInput(!greenhouse.activeInput());
                default -> {
                    int guiFace = payload.action() - GreenhouseConfigPayload.ACTION_FACE_BASE;
                    if (guiFace < 0 || guiFace > 5) {
                        return;
                    }
                    net.minecraft.core.Direction side = menu.guiDirection(guiFace);
                    xiabis.modpack.toolbox.util.GreenhouseFaceMode mode = greenhouse.faceMode(side);
                    greenhouse.setFaceMode(side, mode.next(direction));
                }
            }
            greenhouse.setChanged();
            menu.broadcastChanges();
        });
    }

    private static void handleOpenArmorMenu(OpenArmorMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer player)) {
                return;
            }
            if (!xiabis.modpack.toolbox.util.ArmorSupport.hasArmor(player)) {
                return;
            }
            xiabis.modpack.toolbox.util.ArmorSlotTarget target =
                    xiabis.modpack.toolbox.util.ArmorSupport.resolveTarget(player);
            player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (id, inventory, ignored) -> new xiabis.modpack.toolbox.menu.OmniArmorMenu(id, inventory, target),
                    net.minecraft.network.chat.Component
                            .translatable("gui.xiabis_modpack_toolbox.omni_armor.title")),
                    buffer -> buffer.writeVarInt(target.ordinal()));
        });
    }

    private static void handleSetArmorMarker(SetArmorMarkerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof xiabis.modpack.toolbox.menu.OmniArmorMenu menu) {
                menu.setMarker(payload.slot(), payload.stack().copyWithCount(1));
            }
        });
    }

    private ModNetwork() {
    }
}
