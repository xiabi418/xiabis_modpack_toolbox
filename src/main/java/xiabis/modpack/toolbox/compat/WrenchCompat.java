package xiabis.modpack.toolbox.compat;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Holder;
import xiabis.modpack.toolbox.util.ItemDelegates;

/** Generic wrench support: a tag / class-name proxy item first, then block-state rotation. */
public final class WrenchCompat {
    private static final TagKey<Item> WRENCH_TAG =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
    private static final TagKey<Item> WRENCH_TAG_ALT =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "wrenches"));

    public static InteractionResult wrench(UseOnContext context) {
        Item proxy = findProxy(WRENCH_TAG, WRENCH_TAG_ALT, "wrench", "screwdriver");
        if (proxy != null) {
            InteractionResult result = ItemDelegates.delegateOn(context, proxy);
            if (result.consumesAction()) {
                return result;
            }
        }
        return rotate(context);
    }

    private static InteractionResult rotate(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        BlockState rotated = state.rotate(level, pos, Rotation.CLOCKWISE_90);
        if (rotated == state) {
            return InteractionResult.PASS;
        }
        level.setBlockAndUpdate(pos, rotated);
        level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6F, 1.0F);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static Item findProxy(String tagNamespace, String tagPath, String... nameKeywords) {
        return findProxy(
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(tagNamespace, tagPath)),
                null,
                nameKeywords);
    }

    private static Item findProxy(TagKey<Item> primary, TagKey<Item> secondary, String... nameKeywords) {
        Item tagged = firstInTag(primary);
        if (tagged == null && secondary != null) {
            tagged = firstInTag(secondary);
        }
        if (tagged != null) {
            return tagged;
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (isForeign(item)) {
                String path = BuiltInRegistries.ITEM.getKey(item).getPath();
                String simple = item.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT);
                for (String keyword : nameKeywords) {
                    if (path.contains(keyword) || simple.contains(keyword)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    private static Item firstInTag(TagKey<Item> tag) {
        Optional<HolderSet.Named<Item>> holders = BuiltInRegistries.ITEM.getTag(tag);
        if (holders.isEmpty() || holders.get().size() == 0) {
            return null;
        }
        Holder<Item> holder = holders.get().get(0);
        Item item = holder.value();
        return isForeign(item) ? item : null;
    }

    private static boolean isForeign(Item item) {
        return !(item instanceof xiabis.modpack.toolbox.item.OmniToolItem)
                && !(item instanceof xiabis.modpack.toolbox.item.OmniWeaponItem);
    }

    private WrenchCompat() {
    }
}
