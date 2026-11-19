package xiabis.modpack.toolbox.compat;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Optional Apotheosis integration. The loot category mapping is data driven
 * ({@code data/apotheosis/data_maps/item/loot_category_overrides.json}); this class only seeds a
 * default socket count so gems can be inserted into the omni items.
 */
public final class ApotheosisCompat {
    public static final int DEFAULT_SOCKETS = 4;

    private static DataComponentType<Integer> sockets;
    private static boolean resolved;

    public static void applyDefaults(ItemStack stack) {
        DataComponentType<Integer> type = sockets();
        if (type != null && stack.getOrDefault(type, 0) < DEFAULT_SOCKETS) {
            // Also upgrades stacks that were created while the default was lower.
            stack.set(type, DEFAULT_SOCKETS);
        }
    }

    @SuppressWarnings("unchecked")
    private static DataComponentType<Integer> sockets() {
        if (!resolved) {
            resolved = true;
            if (ModList.get() != null && ModList.get().isLoaded("apotheosis")) {
                try {
                    Class<?> components =
                            Class.forName("dev.shadowsoffire.apotheosis.Apoth$Components");
                    Object value = components.getField("SOCKETS").get(null);
                    if (value instanceof DataComponentType<?> type) {
                        sockets = (DataComponentType<Integer>) type;
                    }
                } catch (ReflectiveOperationException | LinkageError ignored) {
                    sockets = null;
                }
            }
        }
        return sockets;
    }

    private ApotheosisCompat() {
    }
}
