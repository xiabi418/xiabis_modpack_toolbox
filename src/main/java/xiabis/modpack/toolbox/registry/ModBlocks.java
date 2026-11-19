package xiabis.modpack.toolbox.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.block.OmniGeneratorBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(XmtMod.MOD_ID);

    /**
     * Invisible in the world (no item, no loot, unbreakable). It only exists so the accelerated
     * block can be wrapped in a real block model through a {@code minecraft:block_display} entity.
     */
    public static final DeferredBlock<Block> TIME_LAYER = BLOCKS.registerBlock("time_layer", Block::new,
            BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3_600_000.0F)
                    .noLootTable()
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .noTerrainParticles());

    /** Burns solid fuel or lava and pushes the produced energy to its neighbours. */
    public static final DeferredBlock<OmniGeneratorBlock> OMNI_GENERATOR = BLOCKS.registerBlock("omni_generator",
            OmniGeneratorBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(4.0F, 9.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());

    private ModBlocks() {
    }
}
