package xiabis.modpack.toolbox.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import net.neoforged.fml.ModList;
import xiabis.modpack.toolbox.menu.CompressedCondenserMenu;
import xiabis.modpack.toolbox.menu.OmniGeneratorMenu;
import xiabis.modpack.toolbox.menu.FloorBuilderMenu;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, XmtMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<FloorBuilderMenu>> FLOOR_BUILDER =
            MENUS.register("floor_builder", () -> IMenuTypeExtension.create(
                    (id, inventory, buffer) -> new FloorBuilderMenu(id, inventory, buffer.readBlockPos())));

    public static final DeferredHolder<MenuType<?>, MenuType<CompressedCondenserMenu>> COMPRESSED_CONDENSER = ModList.get().isLoaded("ae2")
            ? MENUS.register("compressed_condenser", () -> IMenuTypeExtension.create(CompressedCondenserMenu::fromBuffer))
            : null;

    public static final DeferredHolder<MenuType<?>, MenuType<OmniGeneratorMenu>> OMNI_GENERATOR =
            MENUS.register("omni_generator",
                    () -> IMenuTypeExtension.create(OmniGeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<xiabis.modpack.toolbox.menu.OmniArmorMenu>> OMNI_ARMOR =
            MENUS.register("omni_armor", () -> IMenuTypeExtension.create(
                    (id, inventory, buffer) -> new xiabis.modpack.toolbox.menu.OmniArmorMenu(id, inventory,
                            xiabis.modpack.toolbox.util.ArmorSlotTarget.byOrdinal(buffer.readVarInt()))));

    public static final DeferredHolder<MenuType<?>, MenuType<xiabis.modpack.toolbox.menu.OmniGreenhouseMenu>> OMNI_GREENHOUSE =
            MENUS.register("omni_greenhouse", () -> IMenuTypeExtension.create(
                    xiabis.modpack.toolbox.menu.OmniGreenhouseMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<xiabis.modpack.toolbox.menu.OmniDiggerMenu>> OMNI_DIGGER =
            MENUS.register("omni_digger", () -> IMenuTypeExtension.create(xiabis.modpack.toolbox.menu.OmniDiggerMenu::new));

    private ModMenus() {
    }
}
