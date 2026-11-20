package xiabis.modpack.toolbox.util;

import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

/**
 * Helpers for the oversized Sophisticated Backpacks / Storage containers added by this mod.
 *
 * <p>Both mods keep the inventory outside of the item stack: the stack holds a
 * {@code sophisticatedcore:storage_uuid} component that points at the stored inventory, while slot counts live in
 * {@code number_of_inventory_slots} / {@code number_of_upgrade_slots}. Nothing here needs a compile time
 * dependency on those mods, components are looked up by id at runtime.
 */
public final class StorageUpgradeSupport {
    public static final int UPGRADE_SLOTS = 15;
    public static final int BACKPACK_SLOTS = 4096;
    public static final int BARREL_SLOTS = 8192;

    private static final String BACKPACK_ID = "sophisticatedbackpacks:netherite_backpack";
    private static final String BARREL_ID = "sophisticatedstorage:netherite_barrel";

    private static final Map<String, String> NAMES = Map.of(
            BACKPACK_ID, "库存不足恐惧症",
            BARREL_ID, "容量不足恐惧症");

    private static final ResourceLocation INVENTORY_SLOTS =
            ResourceLocation.parse("sophisticatedcore:number_of_inventory_slots");
    private static final ResourceLocation UPGRADE_SLOT_COUNT =
            ResourceLocation.parse("sophisticatedcore:number_of_upgrade_slots");

    private StorageUpgradeSupport() {
    }

    /** Names the stack the way the recipes describe it: obfuscated markers around a bold red title. */
    public static void applyDisplayName(ItemStack stack) {
        String name = NAMES.get(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (name == null) {
            return;
        }
        MutableComponent marker = Component.literal("AB").withStyle(ChatFormatting.OBFUSCATED);
        MutableComponent display = Component.empty()
                .append(marker.copy())
                .append(Component.literal(name).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(marker.copy());
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                display.withStyle(Style.EMPTY.withItalic(false)));
    }

    /** Writes the oversized slot counts, ignoring the call when the Sophisticated components are absent. */
    public static void applySlotCounts(ItemStack stack, int inventorySlots, int upgradeSlots) {
        setInt(stack, INVENTORY_SLOTS, inventorySlots);
        setInt(stack, UPGRADE_SLOT_COUNT, upgradeSlots);
    }

    /** The upgrade result as shown in the creative tab, or an empty stack when the mod is missing. */
    public static ItemStack creativeBackpack() {
        return creativeStack(BACKPACK_ID, BACKPACK_SLOTS);
    }

    public static ItemStack creativeBarrel() {
        return creativeStack(BARREL_ID, BARREL_SLOTS);
    }

    private static ItemStack creativeStack(String itemId, int inventorySlots) {
        if (ModList.get() != null && !ModList.get().isLoaded(itemId.substring(0, itemId.indexOf(':')))) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        applySlotCounts(stack, inventorySlots, UPGRADE_SLOTS);
        applyDisplayName(stack);
        return stack;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setInt(ItemStack stack, ResourceLocation id, int value) {
        DataComponentType type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
        if (type != null) {
            stack.set(type, value);
        }
    }
}
