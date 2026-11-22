package xiabis.modpack.toolbox.util;

import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLEnvironment;
import xiabis.modpack.toolbox.client.ClientKeyHints;

/**
 * Turns the item configuration key into a tooltip component using the binding the player currently
 * has in the controls screen. Dedicated servers have no key mappings, hence the fallback.
 */
public final class KeyHints {
    private static final Component UNKNOWN = Component.literal("?");

    public static Component toolMenu() {
        return resolve(true);
    }

    public static Component armorMenu() {
        return resolve(false);
    }

    private static Component resolve(boolean tool) {
        try {
            if (FMLEnvironment.dist.isClient()) {
                return tool ? ClientKeyHints.toolMenuKey() : ClientKeyHints.armorMenuKey();
            }
        } catch (Throwable ignored) {
            // No client side key mappings on a dedicated server.
        }
        return UNKNOWN;
    }

    private KeyHints() {
    }
}
