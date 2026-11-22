package xiabis.modpack.toolbox.client;

import net.minecraft.network.chat.Component;

/**
 * Resolves the currently bound keys so tooltips show whatever the player configured in the controls
 * screen instead of a hardcoded letter.
 */
public final class ClientKeyHints {
    public static Component toolMenuKey() {
        return ClientModEvents.OPEN_MENU.getTranslatedKeyMessage();
    }

    public static Component armorMenuKey() {
        return ClientModEvents.OPEN_ARMOR_MENU.getTranslatedKeyMessage();
    }

    private ClientKeyHints() {
    }
}
