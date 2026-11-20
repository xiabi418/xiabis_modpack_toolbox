package xiabis.modpack.toolbox.crafting;

import java.util.Map;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import xiabis.modpack.toolbox.registry.ModRecipes;

/**
 * A normal shaped recipe that carries the "storage identity" of the container placed in its grid over to the
 * result.
 *
 * <p>Sophisticated Backpacks / Sophisticated Storage keep the contents of an item outside the stack itself:
 * the stack only holds a {@code sophisticatedcore:storage_uuid} component that points at the stored inventory
 * (and its upgrades), while the wooden look of a barrel lives in {@code sophisticatedstorage:wood_type}. Copying
 * those components over is therefore enough to keep everything the player had inside, and to keep the wood type
 * of a barrel. Slot counts and the display name come from the recipe JSON and always win over the input's own
 * values.
 */
public class ContainerUpgradeRecipe implements CraftingRecipe {
    /** Components owned by the respective mods; everything else is left alone. */
    private static final String[] COPY_NAMESPACES = {"sophisticatedcore", "sophisticatedstorage", "sophisticatedbackpacks"};
    /** Declared by the recipe JSON, never inherited from the input. */
    private static final String[] KEEP_FROM_RECIPE = {"number_of_inventory_slots", "number_of_upgrade_slots", "custom_name"};

    /** Item id -> display name applied to the crafted container. */
    private static final Map<String, String> RESULT_NAMES = Map.of(
            "sophisticatedbackpacks:netherite_backpack", "库存不足恐惧症",
            "sophisticatedstorage:netherite_barrel", "容量不足恐惧症");

    private final ShapedRecipe compose;

    public ContainerUpgradeRecipe(ShapedRecipe compose) {
        this.compose = compose;
    }

    public ShapedRecipe getCompose() {
        return this.compose;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.compose.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = this.compose.assemble(input, registries);
        ItemStack container = findContainer(input);
        if (container.isEmpty() || result.isEmpty()) {
            return result;
        }
        inheritComponents(container, result);
        applyDisplayName(result);
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void inheritComponents(ItemStack container, ItemStack result) {
        DataComponentPatch patch = container.getComponentsPatch();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            DataComponentType<?> type = entry.getKey();
            ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
            if (id == null || !isCopiedNamespace(id) || isDeclaredByRecipe(id)) {
                continue;
            }
            if (result.has(type)) {
                continue;
            }
            result.set((DataComponentType) type, (Object) entry.getValue().get());
        }
    }

    /**
     * Names the result after the item it upgrades. A styled component is built here instead of being written in
     * the recipe JSON because item components in recipe results only accept plain strings.
     */
    private static void applyDisplayName(ItemStack result) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(result.getItem());
        String name = RESULT_NAMES.get(id.toString());
        if (name == null) {
            return;
        }
        MutableComponent marker = Component.literal("AB").withStyle(ChatFormatting.OBFUSCATED);
        MutableComponent display = Component.empty()
                .append(marker.copy())
                .append(Component.literal(name).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(marker.copy());
        result.set(DataComponents.CUSTOM_NAME, display.withStyle(Style.EMPTY.withItalic(false)));
    }

    private static boolean isCopiedNamespace(ResourceLocation id) {
        for (String namespace : COPY_NAMESPACES) {
            if (namespace.equals(id.getNamespace())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDeclaredByRecipe(ResourceLocation id) {
        for (String path : KEEP_FROM_RECIPE) {
            if (path.equals(id.getPath())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The container is the single ingredient of the pattern that is not a diamond block; the recipes are only
     * ever laid out as a ring of diamond blocks around it.
     */
    private static ItemStack findContainer(CraftingInput input) {
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty() || stack.is(Items.DIAMOND_BLOCK)) {
                continue;
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.compose.getResultItem(registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return this.compose.canCraftInDimensions(width, height);
    }

    @Override
    public CraftingBookCategory category() {
        return this.compose.category();
    }

    @Override
    public String getGroup() {
        return this.compose.getGroup();
    }

    @Override
    public boolean showNotification() {
        return this.compose.showNotification();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CONTAINER_UPGRADE.get();
    }
}
