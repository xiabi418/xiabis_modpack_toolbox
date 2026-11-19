package xiabis.modpack.toolbox.world;

import com.mojang.math.Transformation;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xiabis.modpack.toolbox.registry.ModBlocks;

/**
 * Draws the "this block is accelerated" marker with vanilla display entities, so no custom client
 * code or networking is involved:
 *
 * <ul>
 *   <li>a {@code minecraft:block_display} showing the {@code time_layer} block, scaled to 1.2 and
 *       centred on the block, which produces a shell 0.1 blocks outside every face (no z-fighting);
 *   <li>a {@code minecraft:text_display} floating above the block showing "4x - 59s".
 * </ul>
 */
public final class AccelerationDisplay {
    private static final String TAG = "xmt_accel";
    private static final double SEARCH_RADIUS = 2.0D;
    /** 1.0 block model scaled to 1.2 => extends 0.1 outside the block on every side. */
    private static final float SHELL_SCALE = 1.2F;
    private static final float SHELL_SHIFT = SHELL_SCALE / 2.0F;

    /** Creates the marker if missing and refreshes the countdown when {@code refreshLabel}. */
    public static void sync(ServerLevel level, BlockPos pos, int tier, int secondsLeft,
            boolean refreshLabel) {
        List<Entity> found = markers(level, pos);
        Display.BlockDisplay shell = null;
        Display.TextDisplay label = null;
        for (Entity entity : found) {
            if (entity instanceof Display.BlockDisplay blockDisplay && shell == null) {
                shell = blockDisplay;
            } else if (entity instanceof Display.TextDisplay textDisplay && label == null) {
                label = textDisplay;
            } else {
                entity.discard();
            }
        }
        if (shell == null) {
            spawn(level, shellTag(pos, ModBlocks.TIME_LAYER.get().defaultBlockState()));
        }
        CompoundTag labelTag = labelTag(level, pos, label(tier, secondsLeft));
        if (label == null) {
            spawn(level, labelTag);
        } else if (refreshLabel) {
            // Display setters are private; loading a partial tag is the supported way to update them.
            label.load(labelTag);
        }
    }

    /**
     * Drops every marker in the world. Called on server start: markers are rebuilt from the saved
     * data on the first tick, which removes stale shells whose block expired while unloaded.
     */
    public static void clearAll(net.minecraft.server.MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (entity.getTags().contains(TAG)) {
                    entity.discard();
                }
            }
        }
    }

    /** Removes every marker belonging to {@code pos}. */
    public static void remove(ServerLevel level, BlockPos pos) {
        for (Entity entity : markers(level, pos)) {
            entity.discard();
        }
    }

    private static List<Entity> markers(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(Entity.class, new AABB(pos).inflate(SEARCH_RADIUS),
                entity -> entity.getTags().contains(TAG));
    }

    private static Component label(int tier, int secondsLeft) {
        return Component.translatable("overlay.xiabis_modpack_toolbox.acceleration",
                AccelerationSavedData.MULTIPLIERS[tier] + "x", secondsLeft);
    }

    private static void spawn(ServerLevel level, CompoundTag tag) {
        EntityType.create(tag, level).ifPresent(level::addFreshEntity);
    }

    private static final double LABEL_HEIGHT = 1.15D;

    private static CompoundTag baseTag(String id, BlockPos pos, double yOffset) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.put("Tags", stringList(TAG));
        tag.putBoolean("NoGravity", true);
        tag.putBoolean("Silent", true);
        tag.putBoolean("Invulnerable", true);
        tag.putBoolean("PersistenceRequired", true);
        tag.put("Pos", doubleList(pos.getX() + 0.5D, pos.getY() + yOffset, pos.getZ() + 0.5D));
        return tag;
    }

    private static CompoundTag shellTag(BlockPos pos, BlockState state) {
        CompoundTag tag = baseTag("minecraft:block_display", pos, 0.5D);
        tag.put("block_state", NbtUtils.writeBlockState(state));
        tag.put("transformation", transformation());
        tag.putString("billboard", Display.BillboardConstraints.FIXED.getSerializedName());
        // Always render at full brightness so the marker stays readable in the dark.
        tag.put("brightness", fullBright());
        // Culling box: the shell is 1.2 blocks big, a bit of margin does not hurt.
        tag.putFloat("width", 1.4F);
        tag.putFloat("height", 1.4F);
        return tag;
    }

    private static CompoundTag labelTag(ServerLevel level, BlockPos pos, Component text) {
        CompoundTag tag = baseTag("minecraft:text_display", pos, LABEL_HEIGHT);
        tag.putString("text", Component.Serializer.toJson(text, level.registryAccess()));
        tag.putString("billboard", Display.BillboardConstraints.CENTER.getSerializedName());
        tag.putInt("background", 0x40000000);
        tag.putBoolean("see_through", true);
        tag.putBoolean("shadow", false);
        tag.putInt("line_width", 200);
        tag.putString("alignment", "center");
        tag.putFloat("width", 1.0F);
        tag.putFloat("height", 1.0F);
        return tag;
    }

    /** {-0.6, -0.6, -0.6} then 1.2 scale: a unit block becomes a cube centred on the entity origin. */
    private static Tag transformation() {
        Transformation transformation = new Transformation(
                new Vector3f(-SHELL_SHIFT, -SHELL_SHIFT, -SHELL_SHIFT),
                new Quaternionf(),
                new Vector3f(SHELL_SCALE, SHELL_SCALE, SHELL_SCALE),
                new Quaternionf());
        return Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE, transformation)
                .result()
                .orElseThrow();
    }

    private static CompoundTag fullBright() {
        CompoundTag brightness = new CompoundTag();
        brightness.putInt("block", 15);
        brightness.putInt("sky", 15);
        return brightness;
    }

    private static ListTag doubleList(double x, double y, double z) {
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(x));
        list.add(DoubleTag.valueOf(y));
        list.add(DoubleTag.valueOf(z));
        return list;
    }

    private static ListTag stringList(String... values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf(value));
        }
        return list;
    }

    private AccelerationDisplay() {
    }
}
