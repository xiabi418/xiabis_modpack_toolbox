package xiabis.modpack.toolbox.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class PolishedConcreteBlock extends Block {
    private final DyeColor color;
    private final boolean glowing;

    public PolishedConcreteBlock(BlockBehaviour.Properties properties, DyeColor color, boolean glowing) {
        super(properties.lightLevel(state -> glowing ? 15 : 0));
        this.color = color;
        this.glowing = glowing;
    }

    public DyeColor color() {
        return color;
    }

    public boolean glowing() {
        return glowing;
    }
}
