package xiabis.modpack.toolbox.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.blockentity.OmniGeneratorBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, XmtMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OmniGeneratorBlockEntity>> OMNI_GENERATOR =
            BLOCK_ENTITIES.register("omni_generator", () -> BlockEntityType.Builder
                    .of(OmniGeneratorBlockEntity::new, ModBlocks.OMNI_GENERATOR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<xiabis.modpack.toolbox.blockentity.OmniBatteryBlockEntity>> OMNI_BATTERY =
            BLOCK_ENTITIES.register("omni_battery", () -> BlockEntityType.Builder.of(
                    xiabis.modpack.toolbox.blockentity.OmniBatteryBlockEntity::new,
                    ModBlocks.OMNI_BATTERY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<xiabis.modpack.toolbox.blockentity.OmniGreenhouseBlockEntity>> OMNI_GREENHOUSE =
            BLOCK_ENTITIES.register("omni_greenhouse", () -> BlockEntityType.Builder.of(
                    xiabis.modpack.toolbox.blockentity.OmniGreenhouseBlockEntity::new,
                    ModBlocks.OMNI_GREENHOUSE.get()).build(null));

    private ModBlockEntities() {
    }
}
