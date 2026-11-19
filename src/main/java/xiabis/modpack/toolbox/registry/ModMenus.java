package xiabis.modpack.toolbox.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.menu.OmniGeneratorMenu;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, XmtMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<OmniGeneratorMenu>> OMNI_GENERATOR =
            MENUS.register("omni_generator",
                    () -> IMenuTypeExtension.create(OmniGeneratorMenu::new));

    private ModMenus() {
    }
}
