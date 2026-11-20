package xiabis.modpack.toolbox.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.crafting.ContainerUpgradeRecipeSerializer;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, XmtMod.MOD_ID);

    public static final Supplier<ContainerUpgradeRecipeSerializer> CONTAINER_UPGRADE =
            RECIPE_SERIALIZERS.register("container_upgrade", ContainerUpgradeRecipeSerializer::new);

    private ModRecipes() {
    }
}
