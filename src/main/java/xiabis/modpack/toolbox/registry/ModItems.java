package xiabis.modpack.toolbox.registry;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiabis.modpack.toolbox.XmtMod;
import xiabis.modpack.toolbox.item.AdvancedTotemItem;
import xiabis.modpack.toolbox.item.MagicPoisonBottleItem;
import xiabis.modpack.toolbox.item.OmniFluidTankBlockItem;
import xiabis.modpack.toolbox.item.OmniToolItem;
import xiabis.modpack.toolbox.item.OmniWeaponItem;
import xiabis.modpack.toolbox.item.TimeWandItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(XmtMod.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, XmtMod.MOD_ID);

    public static final DeferredItem<Item> OMNI_TOOL = ITEMS.registerItem("omni_tool", OmniToolItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .durability(OmniToolItem.DURABILITY)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));

    public static final DeferredItem<Item> OMNI_WEAPON = ITEMS.registerItem("omni_weapon", OmniWeaponItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .durability(OmniWeaponItem.DURABILITY)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));

    public static final DeferredItem<Item> OMNI_ARMOR = ITEMS.registerItem("omni_armor",
            xiabis.modpack.toolbox.item.OmniArmorItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .durability(xiabis.modpack.toolbox.item.OmniArmorItem.DURABILITY)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));

    public static final DeferredItem<Item> TIME_WAND = ITEMS.registerItem("time_wand", TimeWandItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

    public static final DeferredItem<BlockItem> COMPRESSED_CONDENSER_ITEM = ModList.get().isLoaded("ae2")
            ? ITEMS.registerSimpleBlockItem(ModBlocks.COMPRESSED_CONDENSER) : null;

    public static final DeferredItem<BlockItem> OMNI_GENERATOR_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.OMNI_GENERATOR);

    public static final DeferredItem<BlockItem> OMNI_FLUID_TANK_ITEM = ITEMS.registerItem("omni_fluid_tank",
            properties -> new OmniFluidTankBlockItem(ModBlocks.OMNI_FLUID_TANK.get(), properties), new Item.Properties());

    /** Block item of the giant battery box. */
    public static final DeferredItem<BlockItem> OMNI_BATTERY_ITEM = ITEMS.registerItem("omni_battery",
            properties -> new xiabis.modpack.toolbox.item.OmniBatteryBlockItem(ModBlocks.OMNI_BATTERY.get(), properties),
            new Item.Properties());

    /** Block item of the omni greenhouse. */
    public static final DeferredItem<BlockItem> OMNI_DIGGER_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.OMNI_DIGGER);
    public static final DeferredItem<BlockItem> OMNI_GREENHOUSE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.OMNI_GREENHOUSE);

    public static final DeferredItem<BlockItem> OVERWORLD_TELEPORT_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.OVERWORLD_TELEPORT);
    public static final DeferredItem<BlockItem> FLOOR_BUILDER_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.FLOOR_BUILDER);
    public static final DeferredItem<BlockItem> NETHER_TELEPORT_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.NETHER_TELEPORT);
    public static final DeferredItem<BlockItem> END_TELEPORT_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.END_TELEPORT);

    public static final Map<DyeColor, DeferredItem<BlockItem>> POLISHED_CONCRETE = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredItem<BlockItem>> GLOWING_POLISHED_CONCRETE = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            POLISHED_CONCRETE.put(color, ITEMS.registerSimpleBlockItem(ModBlocks.POLISHED_CONCRETE.get(color)));
            GLOWING_POLISHED_CONCRETE.put(color, ITEMS.registerSimpleBlockItem(ModBlocks.GLOWING_POLISHED_CONCRETE.get(color)));
        }
    }

    /** Upgrade card that doubles the generator's internal energy buffer per card. */
    public static final DeferredItem<Item> ENERGY_CARD = ITEMS.registerItem("energy_card", Item::new,
            new Item.Properties().stacksTo(64).rarity(Rarity.RARE));

    /** Upgrade card that doubles the generator's generation and output rate per card. */
    public static final DeferredItem<Item> SPEED_CARD = ITEMS.registerItem("speed_card", Item::new,
            new Item.Properties().stacksTo(64).rarity(Rarity.RARE));

    public static final DeferredItem<Item> ADVANCED_TOTEM_OF_UNDYING = ITEMS.registerItem(
            "advanced_totem_of_undying", AdvancedTotemItem::new,
            new Item.Properties().stacksTo(1).durability(AdvancedTotemItem.DURABILITY).rarity(Rarity.EPIC));

    public static final DeferredItem<Item> MAGIC_POISON_BOTTLE = ITEMS.registerItem("magic_poison_bottle",
            MagicPoisonBottleItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    /**
     * The 24 hour flight potion item, or an empty stack when Apothic Attributes (and therefore the
     * "flying" effect) is not installed.
     */
    public static ItemStack flightPotion() {
        if (!ModPotions.hasFlightEffect()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(net.minecraft.world.item.Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS,
                new net.minecraft.world.item.alchemy.PotionContents(ModPotions.FLIGHT));
        return stack;
    }

    public static final int FLIGHT_POTION_DURATION_TICKS = ModPotions.FLIGHT_DURATION_TICKS;

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_TABS.register("toolbox", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.xiabis_modpack_toolbox.toolbox"))
                    .icon(() -> new ItemStack(OMNI_TOOL.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(OMNI_TOOL.get());
                        output.accept(OMNI_WEAPON.get());
                        output.accept(OMNI_ARMOR.get());
                        output.accept(TIME_WAND.get());
                        output.accept(MAGIC_POISON_BOTTLE.get());
                        output.accept(ADVANCED_TOTEM_OF_UNDYING.get());
                        output.accept(OMNI_GENERATOR_ITEM.get());
                        output.accept(OMNI_FLUID_TANK_ITEM.get());
                        output.accept(ENERGY_CARD.get());
                        output.accept(SPEED_CARD.get());
                        output.accept(OMNI_BATTERY_ITEM.get());
                        output.accept(OMNI_GREENHOUSE_ITEM.get());
                         output.accept(OMNI_DIGGER_ITEM.get());
                        output.accept(OVERWORLD_TELEPORT_ITEM.get());
                         output.accept(FLOOR_BUILDER_ITEM.get());
                        output.accept(NETHER_TELEPORT_ITEM.get());
                        output.accept(END_TELEPORT_ITEM.get());
                        if (COMPRESSED_CONDENSER_ITEM != null) {
                            output.accept(COMPRESSED_CONDENSER_ITEM.get());
                        }

                        ItemStack bigBackpack = xiabis.modpack.toolbox.util.StorageUpgradeSupport.creativeBackpack();
                        if (!bigBackpack.isEmpty()) {
                            output.accept(bigBackpack);
                        }
                        ItemStack bigBarrel = xiabis.modpack.toolbox.util.StorageUpgradeSupport.creativeBarrel();
                        if (!bigBarrel.isEmpty()) {
                            output.accept(bigBarrel);
                        }
                        ItemStack flightPotion = flightPotion();
                        if (!flightPotion.isEmpty()) {
                            output.accept(flightPotion);
                        }
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BUILDING_TAB =
            CREATIVE_TABS.register("building", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.xiabis_modpack_toolbox.building"))
                    .icon(() -> new ItemStack(GLOWING_POLISHED_CONCRETE.get(DyeColor.WHITE).get()))
                    .displayItems((parameters, output) -> {
                        for (DyeColor color : DyeColor.values()) {
                            output.accept(POLISHED_CONCRETE.get(color).get());
                            output.accept(GLOWING_POLISHED_CONCRETE.get(color).get());
                        }
                    })
                    .build());

    private ModItems() {
    }
}
