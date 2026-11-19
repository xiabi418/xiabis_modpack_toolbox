package xiabis.modpack.toolbox.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

/** Runs a vanilla/modded item's {@code useOn} with a throw-away stack. */
public final class ItemDelegates {
    public static InteractionResult delegateOn(UseOnContext context, Item proxy) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockHitResult hit = new BlockHitResult(
                context.getClickLocation(),
                context.getClickedFace(),
                context.getClickedPos(),
                context.isInside());
        UseOnContext delegateContext =
                new UseOnContext(context.getLevel(), player, context.getHand(), new ItemStack(proxy), hit);
        return proxy.useOn(delegateContext);
    }

    private ItemDelegates() {
    }
}
