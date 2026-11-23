package xiabis.modpack.toolbox.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import net.neoforged.fml.ModList;
import xiabis.modpack.toolbox.block.CompressedCondenserBlock;
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

    public static final DeferredBlock<Block> COMPRESSED_CONDENSER = ModList.get().isLoaded("ae2")
            ? BLOCKS.registerBlock("compressed_condenser", CompressedCondenserBlock::new,
                    BlockBehaviour.Properties.of().strength(4.0F, 9.0F).requiresCorrectToolForDrops().noOcclusion())
            : null;

    public static final DeferredBlock<OmniGeneratorBlock> OMNI_GENERATOR = BLOCKS.registerBlock("omni_generator",
            OmniGeneratorBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(4.0F, 9.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());


    /** Grows one seed or sapling into its harvest while drinking water and FE. */
    public static final DeferredBlock<xiabis.modpack.toolbox.block.OmniGreenhouseBlock> OMNI_GREENHOUSE =
            BLOCKS.registerBlock("omni_greenhouse", xiabis.modpack.toolbox.block.OmniGreenhouseBlock::new,
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                            .strength(4.0F, 9.0F)
                            .requiresCorrectToolForDrops()
                            .noOcclusion());

    /** Giant battery box: unbounded FE buffer, configured per side. */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> OMNI_BATTERY =
            BLOCKS.registerBlock("omni_battery", xiabis.modpack.toolbox.block.OmniBatteryBlock::new,
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                            .strength(4.0F, 9.0F)
                            .requiresCorrectToolForDrops()
                            .noOcclusion());

    private ModBlocks() {
    }
}
