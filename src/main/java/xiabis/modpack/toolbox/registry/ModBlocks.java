package xiabis.modpack.toolbox.registry;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.block.CompressedCondenserBlock;
import xiabis.modpack.toolbox.block.OmniFluidTankBlock;
import xiabis.modpack.toolbox.block.OmniGeneratorBlock;
import xiabis.modpack.toolbox.block.PolishedConcreteBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(XmtMod.MOD_ID);

    public static final DeferredBlock<Block> TIME_LAYER = BLOCKS.registerBlock("time_layer", Block::new,
            BlockBehaviour.Properties.of().noCollission().noOcclusion().strength(-1.0F, 3_600_000.0F)
                    .noLootTable().isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false).noTerrainParticles());

    public static final DeferredBlock<Block> COMPRESSED_CONDENSER = ModList.get().isLoaded("ae2")
            ? BLOCKS.registerBlock("compressed_condenser", CompressedCondenserBlock::new,
                    BlockBehaviour.Properties.of().strength(4.0F, 9.0F).requiresCorrectToolForDrops().noOcclusion())
            : null;

    public static final DeferredBlock<OmniGeneratorBlock> OMNI_GENERATOR = BLOCKS.registerBlock("omni_generator",
            OmniGeneratorBlock::new, machineProperties());
    public static final DeferredBlock<OmniFluidTankBlock> OMNI_FLUID_TANK = BLOCKS.registerBlock("omni_fluid_tank",
            OmniFluidTankBlock::new, machineProperties());
    public static final DeferredBlock<xiabis.modpack.toolbox.block.OmniGreenhouseBlock> OMNI_GREENHOUSE =
            BLOCKS.registerBlock("omni_greenhouse", xiabis.modpack.toolbox.block.OmniGreenhouseBlock::new, machineProperties());
    public static final DeferredBlock<Block> OMNI_BATTERY = BLOCKS.registerBlock("omni_battery",
            xiabis.modpack.toolbox.block.OmniBatteryBlock::new, machineProperties());

    public static final Map<DyeColor, DeferredBlock<PolishedConcreteBlock>> POLISHED_CONCRETE = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<PolishedConcreteBlock>> GLOWING_POLISHED_CONCRETE = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName();
            POLISHED_CONCRETE.put(color, BLOCKS.registerBlock(name + "_polished_concrete",
                    properties -> new PolishedConcreteBlock(properties, color, false), concreteProperties()));
            GLOWING_POLISHED_CONCRETE.put(color, BLOCKS.registerBlock("glowing_" + name + "_polished_concrete",
                    properties -> new PolishedConcreteBlock(properties, color, true), concreteProperties()));
        }
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().strength(4.0F, 9.0F).requiresCorrectToolForDrops().noOcclusion();
    }

    private static BlockBehaviour.Properties concreteProperties() {
        return BlockBehaviour.Properties.of().strength(1.8F, 6.0F).requiresCorrectToolForDrops();
    }

    private ModBlocks() {
    }
}
