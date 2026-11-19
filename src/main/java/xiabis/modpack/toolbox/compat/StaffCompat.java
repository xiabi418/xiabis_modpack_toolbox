package xiabis.modpack.toolbox.compat;

import java.lang.reflect.Method;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

/**
 * Staff support.
 *
 * <p>With Iron's Spells 'n Spellbooks installed the staff casts exactly like a spell book:
 * the server side cast entry point ({@code Utils#serverSideInitiateCast}) resolves the spell the
 * player selected (spell book in the curios slot, scrolls, ...) and runs the normal
 * mana / cooldown / animation pipeline.
 *
 * <p>Otherwise it falls back to "borrowing" any staff / wand / spell item and calling its use().
 */
public final class StaffCompat {
    private static final String IRONS_UTILS = "io.redspace.ironsspellbooks.api.util.Utils";
    private static Method ironCast;
    private static boolean ironResolved;

    public static InteractionResult cast(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }
        if (castIronsSpell(player)) {
            return InteractionResult.SUCCESS;
        }
        Item proxy = WrenchCompat.findProxy("c", "tools/staff", "staff", "wand", "spellbook", "grimoire");
        if (proxy == null) {
            return InteractionResult.PASS;
        }
        ItemStack real = player.getItemInHand(hand);
        if (real.getItem() == proxy) {
            return InteractionResult.PASS;
        }
        player.setItemInHand(hand, new ItemStack(proxy));
        try {
            InteractionResultHolder<ItemStack> result = proxy.use(level, player, hand);
            return result.getResult();
        } finally {
            player.setItemInHand(hand, real);
        }
    }

    /** Casts the currently selected Iron's Spells spell, if that mod is present. */
    private static boolean castIronsSpell(Player player) {
        Method method = ironCastMethod();
        if (method == null || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(method.invoke(null, serverPlayer));
        } catch (ReflectiveOperationException | LinkageError exception) {
            return false;
        }
    }

    private static Method ironCastMethod() {
        if (!ironResolved) {
            ironResolved = true;
            if (ModList.get().isLoaded("irons_spellbooks")) {
                try {
                    Class<?> utils = Class.forName(IRONS_UTILS);
                    ironCast = utils.getMethod("serverSideInitiateCast", ServerPlayer.class);
                } catch (ReflectiveOperationException | LinkageError exception) {
                    ironCast = null;
                }
            }
        }
        return ironCast;
    }

    private StaffCompat() {
    }
}
