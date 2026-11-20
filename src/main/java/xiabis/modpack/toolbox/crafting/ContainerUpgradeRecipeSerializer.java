package xiabis.modpack.toolbox.crafting;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

/** Reads/writes {@link ContainerUpgradeRecipe} as a plain shaped recipe so recipe viewers can render it. */
public class ContainerUpgradeRecipeSerializer implements RecipeSerializer<ContainerUpgradeRecipe> {
    private static final MapCodec<ContainerUpgradeRecipe> CODEC =
            ShapedRecipe.Serializer.CODEC.xmap(ContainerUpgradeRecipe::new, ContainerUpgradeRecipe::getCompose);

    private static final StreamCodec<RegistryFriendlyByteBuf, ContainerUpgradeRecipe> STREAM_CODEC =
            ShapedRecipe.Serializer.STREAM_CODEC.map(ContainerUpgradeRecipe::new, ContainerUpgradeRecipe::getCompose);

    @Override
    public MapCodec<ContainerUpgradeRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ContainerUpgradeRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
