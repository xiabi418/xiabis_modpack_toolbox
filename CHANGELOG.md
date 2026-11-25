# Changelog

All notable changes to xiabi's modpack toolbox are recorded here.

## 2.6.1

- Fixed the three teleport block recipes not loading on Minecraft 1.21.1.
- Preserve the return block when a cross-dimension return is canceled, and remove unused return blocks if entry is canceled.

## 2.6.0

- Added Overworld, Nether, and End private-dimension teleport blocks.
- Added automatic owner-bound return blocks at safe destination landing points; return blocks have no recipe and are consumed on successful return.
- Added nine-dirt, nine-netherrack, and nine-end-stone recipes for the three entry blocks.

## 2.5.1

- Filled the full private Overworld below Y=63 to remove terrain and aquifer caves.
- Reapply the specified solid Nether and End foundations after carvers run.

## 2.5.0

- Added private Overworld, Nether, and End dimensions that share vanilla biome sources and generation settings.
- Added a flat Y=63 private Overworld without cave carvers, a solid Nether foundation through Y=128, and a solid End foundation through Y=128 with a bedrock floor and no main island terrain.
- Kept biome-based mod features and structures in the normal generation pipeline; mods hard-coded to vanilla dimension IDs are not universally compatible.

## 2.4.3

- Moved all 32 polished concrete variants into the dedicated building-materials creative tab.
- Set the tab icon to Glowing White Polished Concrete.


- Fixed startup without Applied Energistics 2 by isolating the Omni Fluid Tank storage-bus capability registration.

## 2.4.1

- Corrected the concrete crafting recipe to consume nine regular concrete blocks.
- Repacked the connected textures into Fusion’s five-tile layout and added pickaxe mining support.
- Restored existing Simplified Chinese translations and documentation headings.

## 2.4.0

- Added 16 tinted polished concrete blocks and 16 glowing variants.
- Added Fusion connected-texture rendering using the supplied concrete texture sheets.
- Added 1:1 stonecutting, nine-output crafting, cobblestone duplication, and glowstone upgrade recipes.
- Glowing variants emit light level 15.

## 2.3.4

- Preserved Omni Fluid Tank contents when mined and restored them when placed again.
- Added per-fluid tank item tooltips with infinite and partial-capacity displays.

## 2.3.3

- Restored the Omni Greenhouse to the toolbox creative tab.

## 2.3.2

- Converted the Simplified Chinese locale to readable UTF-8 text.
- Removed the Advanced Totem of Undying tooltip.

## 2.3.1

- Added custom textures for the Omni Fluid Tank and Advanced Totem of Undying.

## 2.3.0

- Added the Omni Fluid Tank (`omni_fluid_tank`) with unlimited fluid types and 8-bucket storage per type.
- Full fluids can be extracted without depletion; same-fluid overflow is voided.
- Added optional AE2 storage-bus exposure at 2.1G per full fluid type, plus recipe, models and translations.

## 2.2.0

- Added the Advanced Totem of Undying (`advanced_totem_of_undying`) with 256 durability, vanilla rescue effects and the requested bonus effects.
- Added its shaped recipe and English/Simplified Chinese names, tooltip and guide sections.

## 2.1.0

- Added the optional AE2 Compressed Matter Condenser (`compressed_condenser`).
- Added ME-network extraction, storage-cell output, AE2-value aggregation, six card slots and matter-ball/singularity modes.
- Added English and Simplified Chinese player-guide sections.
- AE2 API compilation is downloaded on demand; AE2 remains optional and is not bundled in the release jar.


- Replaced the README with separate English and Simplified Chinese player guides.
- Moved the version history into this standalone changelog.


- Reduced the server work used to keep time-acceleration markers updated.
- Greenhouse production now pauses early when every output slot is full, without rolling drops or consuming water and energy.
- Optional integration mods are only loaded for local testing when their files are present.

## 1.6.4

- The greenhouse stops working when its output slots cannot hold a complete harvest.
- No water or energy is consumed and no items are dropped while the outputs are full.

## 1.6.3

- Moved all GUI item slots upward by one pixel.
- Added greenhouse support for nether fungi, mushrooms, sugar cane, bamboo, cactus, kelp, berries, cocoa beans, sea pickles and chorus fruit.
- Improved Mystical Agriculture harvests so crops can produce their essence again.

## 1.6.2

- Fine-tuned the generator, armor and greenhouse GUI slot positions to match the artwork.
- Replaced the greenhouse GUI artwork with the supplied version.

## 1.6.1

- Corrected the GUI canvas sizes so all three interfaces render at the intended scale.

## 1.6.0

- Added the Large Omni Greenhouse (`omni_greenhouse`).
- Added crop and sapling production, water and energy storage, nine output slots, side configuration, Speed Cards and Energy Cards.

## 1.5.0

- Added the supplied Omni Armor model and item artwork.
- Added the supplied generator and armor GUI artwork.
- Adjusted the interfaces to align with the hand-drawn slot and button layouts.

## 1.4.2

- Armor and toughness are now shown through the normal item attribute display.
- Tool and armor configuration hints now follow the currently assigned key bindings.

## 1.4.1

- Fixed the Giant Battery Box rendering after placement.
- Changed the armor marker grid to ghost slots so marking blocks never consumes or duplicates items.

## 1.4.0

- Added Omni Armor (`omni_armor`) with vanilla and Curios head-slot support.
- Added armor protection, fog removal, damage protection and the block-highlighting configuration screen.

## 1.3.11

- Improved compatibility with newer AE2 energy output behavior.
- Protected optional AE2 and GregTech energy adapters from interrupting generator ticks.

## 1.3.8–1.3.10

- Added the Giant Battery Box (`omni_battery`).
- Increased battery storage and transfer limits.
- Battery charge is preserved when the block is broken and placed again.

## 1.3.7

- Added Sophisticated Backpacks and Sophisticated Storage upgrade recipes.
- Added oversized Netherite containers with retained contents, wood style and expanded slots.

## 1.3.6

- Added the supplied generator block textures, including a dedicated bottom texture.

## 1.3.5

- Added the supplied Energy Card and Speed Card artwork.
- Added the mod icon and issue-tracker link.

## 1.3.4

- Updated the mod metadata supplied by the maintainer.

## 1.3.3

- Successfully marked effects are now consumed from the player.
- Effects already stored at the same or higher level are not consumed.

## 1.3.2

- Removed mining functions from the Omni Weapon. Mining functions remain exclusive to the Omni Tool.
- Fixed legacy Omni Weapon items so they migrate to the correct weapon behavior.

## 1.3.1

- Replaced custom reach and magic tooltip lines with the normal Minecraft attribute display.
- Kept the Iron's Spellbooks bonuses visible through the standard item attributes.

## 1.3.0

- Added the Omni Generator (`omni_generator`).
- Added FE, EU and AE output options, six-side routing, fuel and lava input, Energy Cards and Speed Cards.

## 1.2.3

- Added common vanilla and modded item tags for the Omni Tool and Omni Weapon.

## 1.2.2

- Added block and entity reach bonuses.
- Added Iron's Spellbooks mana and spell bonuses when the Omni Tool is held.
- Changed the shared mining time to approximately 0.3 seconds per block.

## 1.2.1

- Replaced the unreliable acceleration text renderer with a visible block shell and floating countdown marker.
- Added the supplied Time Wand and time-layer artwork.

## 1.2.0

- Added the Time Acceleration Wand (`time_wand`).
- Added 4x through 256x acceleration for block entities and randomly ticking blocks.
- Updated the Magic Poison Bottle recipe.
- Fixed the Flight Potion recipe so it uses real Water Bottles.

## 1.1.1

- Added the Curios player charm-slot mapping so the Magic Poison Bottle can be equipped normally.
- Added mining fallback behavior and standard mining information for the Omni items.

## 1.1.0

- Added the Magic Poison Bottle (`magic_poison_bottle`).
- Added effect marking, infinite effects while worn and the Curios charm integration.
- Added the 24-hour Flight Potion recipe using Apothic Attributes' flight effect.

## 1.0.1

- Improved the Omni Weapon's mace smash, Multishot, Quick Charge and bow-style behavior.
- Added four weapon draw-stage textures.
- Added sonic-boom blocking in shield mode and Iron's Spellbooks staff casting.

## 1.0.0

- Initial release with the Omni Tool and Omni Weapon.
## 2.0.0

* Reduced server-side work for time-wand markers: display entities are reconciled once per second instead of
  being searched and refreshed for every accelerated block on every tick. A newly applied marker is still
  created immediately by the wand.
* Greenhouse cycles now short-circuit before crop/loot generation when all nine output slots are full. The
  existing full-batch simulation remains as the final guard, so a cycle consumes no water/FE and never spills
  items into the world when its outputs cannot hold the complete result.
* Optional integration jars used for local testing are now `runtimeOnly` dependencies. They remain available in
  the configured development `libs/` directory, but a clean clone only needs the required Curios download to
  compile the mod; the optional integrations are still discovered at runtime when their jars are present.

## 1.6.4

* The greenhouse no longer works when its nine output slots are full: it holds the finished cycle, consumes
  no energy or water and never throws the harvest on the ground. Once a slot is emptied it resumes on the
  next retry tick (checked every 0.5 s while blocked).

## 1.6.3

* Every container slot moved up one more pixel so 16x16 item sprites sit centred in the drawn 16x14 cells
  instead of hanging over the bottom bevel.
* The greenhouse seed slot now also accepts nether fungi, mushrooms, sugar cane, bamboo, cactus, kelp,
  sweet berries, glow berries, cocoa beans, sea pickles and chorus fruit, each with its own harvest table
  (nether fungi give shroomlight, wart blocks and stems).
* Greenhouse harvests are collected with `Block#getDrops` instead of rolling the block loot table, so
  Mystical Agriculture crops hand out their essence again (they generate drops in code, not in a loot
  table).

## 1.6.2

* Fine tuned both container GUIs against the drawn art: the 3x3 config buttons now fill exactly the
  drawn button cells (16x14 at 185/30 instead of 18x16 at 184/29), the generator card slots moved onto
  their drawn cells (102/120, 65 instead of 101/120, 63), and every item slot was nudged up one pixel so
  the 16x16 item sprites sit centered in the drawn 16x14 cells instead of spilling over the lower bevel.
* The greenhouse GUI now uses the maintainer's hand drawn sheet: two gauges (water left, FE right at
  33/30), the seed slot at 60/25, three by three outputs at 114/29, two card slots at 64/65 and 82/65,
  and the progress bar at 64/56.

## 1.6.1

* Fixed the container GUIs being distorted: the three GUI sheets were saved at their drawn size
  (245x186 / 176x186) while `blit` still declared a 256x256 sheet, so Minecraft sampled only the top
  73 percent of the texture and stretched it. All three are now stored on a 256x256 canvas at the
  original coordinates, so the art lines up with the slot positions again.

## 1.6.0

* Added the omni greenhouse (`omni_greenhouse`) with its GUI, face configuration, water/FE handling, loot table
  based harvests (crops, saplings, Mystical Agriculture style seeds) and the speed/energy card support.

## 1.5.0

* Replaced the placeholder art with the hand-drawn assets: the omni armor's worn model
  (`textures/models/armor/omni_armor_layer_1.png`), its item icon, and both container GUIs.
* The armor marker screen now uses the drawn 176x186 sheet (title bar, 4x9 marker grid, inventory and
  hotbar all line up with the art).
* The generator screen now uses the drawn 176x186 panel plus the 3x3 button cluster on its right
  (245x186 total): fuel slots at the drawn 3x3 grid, the two card slots sit on the drawn card icons,
  the lava and energy gauges fill the drawn frames and the burn progress fills the thin bar.
  Button colours now indicate state (green = active toggle, blue = fluid input face, orange = item
  input face, red = energy output face, green = both) and hovering a gauge shows the exact amount.

## 1.4.2

* `omni_armor` now exposes armor and toughness as regular item attribute modifiers, so the vanilla
  tooltip shows `When on Head: +60 Armor / +60 Armor Toughness`; the custom "wear it for +60" line is
  gone. Wearing the armor in the Curios head slot still works through a server-side fallback modifier.
* Both `omni_tool` and `omni_armor` now show `Press <key> to open the item configuration screen`
  (translated, `%s` = current binding). The key text follows the controls screen, and both menu keys
  (`Open Tool Menu`, `Open armor highlight menu`) already appear under Options > Controls >
  xiabi's Modpack Toolbox, so they can be rebound there.

## 1.4.1

* Fixed the **Giant Battery Box** being invisible once placed (only its item looked right). The block
  extends `BaseEntityBlock`, whose default render shape is `INVISIBLE`; it now returns
  `RenderShape.MODEL` like the generator does.
* The armor marker grid is now made of ghost slots: clicking marks the held block without consuming
  it, JEI drags can no longer be pulled back out (item duplication), shift-clicking never moves items
  into the grid, and clicking a marker with an empty hand clears it.

## 1.4.0

* New item **Omni Armor** (`omni_armor`): helmet/Curios head slot, +60 armor and +60 toughness,
  640 durability with `Unbreakable`, Protection 20 / Respiration 20 / Aqua Affinity 1, immunity to
  every damage source except `/kill`, full fog removal (including Darkness/Blindness) and a 36 slot
  block-highlight editor opened with `H` that accepts inventory and JEI drags.
* New client-side outline renderer for highlighted blocks (custom `RenderType`, no depth test, 12
  colour palette, 32 block radius, half-second refresh).
* New packet pair for the marker menu (`open_armor_menu`, `set_armor_marker`) and the `marker_slots`
  item component.
* Curios `player` entity now declares the `charm` and `head` slots; `omni_armor` is tagged
  `curios:head`.

## 1.3.11

* Fixed a server crash (`No enum constant appeng.api.config.Actionable.MODIFY`) when the generator
  was set to AE output with AE2 19.x installed. AE2 renamed `Actionable.MODIFY` to
  `Actionable.MODULATE`; the adapter now resolves the constant by scanning the enum (accepts both
  names) and disables itself with a logged warning if the API changed again.
* Soft-dependency adapters (AE2 / GregTech) can no longer throw out of a block entity tick: the
  generator's energy push is wrapped in a guard that logs and skips the face.

## 1.3.10

* Active I/O transfer is now exactly `Integer.MAX_VALUE` (2 147 483 647 FE/t per side).

## 1.3.9

* Battery capacity raised from `Integer.MAX_VALUE` to a real `long` (`Long.MAX_VALUE`); the FE
  capability still reports clamped `int` values.
* Active I/O transfer raised from 1 000 000 FE/t to `Integer.MAX_VALUE` FE/t per side.
* Breaking a giant battery box keeps its stored energy: it is written into the dropped item
  (`stored_energy` component) and restored on placement, with a tooltip showing the amount.

## 1.3.8

* New block **Giant Battery Box** (`omni_battery`): unbounded FE storage, per-face input/output
  mode, active I/O toggle, action bar feedback, no GUI.
* The oversized Sophisticated Backpacks / Storage containers are now listed in this mod's creative
  tab when their mod is installed (with the obfuscated red names and the 4096/8192 slot counts).

## 1.3.4 - 1.3.7

* 1.3.4: the mods.toml is now maintained by the author (only `version` stays templated).
* 1.3.5: mod logo (`icon.png`), issue tracker URL and the provided card textures.
* 1.3.6: provided generator block textures, including a dedicated bottom face.
* 1.3.7: storage upgrade recipes for Sophisticated Backpacks / Storage (8 diamond blocks around a
  diamond container -> oversized netherite container that keeps its contents).

## 1.3.3

* The magic poison bottle now **consumes** an effect when marking it succeeds: the marked effect is
  removed from the player. Effects that are already marked at the same (or a higher) level, and
  instances weaker than the marked level, are left alone.
* Example: with Speed II and Jump Boost I you mark both, both are stored at level 2 / 1 and removed
  from you. Drinking Speed I later and marking again does nothing (already marked at a higher
  level), while Speed III raises the stored level to 3 and is consumed.

## 1.3.2

* Fixed the omni weapon providing mining: it no longer carries the omni tool's mining rules.
  The weapon keeps only the sword's block interaction (cobwebs at 15x, sword-efficient blocks at
  1.5x) and cannot harvest any block that needs a pickaxe/shovel/hoe/shears, so stone, ores and
  logs give no drops and break at normal hand speed.
* The custom 0.3 s break speed now only applies to the **omni tool**; the weapon is not affected.
* Weapon stacks from older versions are migrated on first tick (their old 8-rule mining `tool`
  component is replaced with the 2-rule sword component).
* The weapon was also removed from the `c:tools/mining_tool`, `minecraft:cluster_max_harvestables`,
  `minecraft:enchantable/mining` and `minecraft:enchantable/mining_loot` tags.

## 1.3.1

* Removed the custom tooltip lines from both omni items (the "Mining tier", "Mining speed" and
  reach lines are gone).
* Reach and Iron's Spellbooks bonuses are now **real item attribute modifiers** built in
  `getDefaultAttributeModifiers`, so the stock tooltip renders them under
  "When in Main Hand:" / "When held:" and no per-tick player patching is needed
  (`PlayerAttributesHandler` was deleted).
* Iron's Spellbooks defines spell power, cooldown reduction and mana regeneration as percentage
  attributes (base 1.0 = 100%), therefore the +20 spell power addition shows up as
  "+2000% Spell Power" (total 21.0) while max mana shows as a flat "+5000 Max Mana".

## 1.3.0

* New block `omni_generator` (万用发电机) with its GUI, face routing, fuel / lava burning,
  FE / EU / AE output and the two upgrade cards `energy_card` and `speed_card`
  (see the Omni Generator section above).

## 1.2.3

* Added the vanilla `minecraft:*` and NeoForge `c:*` item tags listed above so modpacks and
  other mods detect the omni tool / omni weapon as tools and weapons.

## 1.2.2

* Omni Tool: **+5 block interaction range** in the main hand; with Iron's Spellbooks also
  **+20 spell power**, **+5000 max mana**, **+500% spell cooldown speed** (attribute 1.0 -> 6.0)
  and **+500% mana regeneration** (1.0 -> 6.0) while held in either hand.
* Omni Weapon: **+4 entity interaction range** in the main hand.
* Digging time for both items changed from 18 ticks (0.9 s) to **6 ticks (0.3 s)** per block.
* Range and magic bonuses are item attribute modifiers now, so the vanilla tooltip shows them
  (the magic entries only appear when Iron's is loaded).

## 1.2.1

* The acceleration marker is now a real block shell plus a floating countdown text entity instead
  of a client side text renderer that did not show up in game: a `minecraft:block_display` shows
  the internal `time_layer` block scaled 1.2 (0.1 blocks outside the face, no z-fighting, full
  brightness) and a `minecraft:text_display` shows `4x - 59s`. The old client sync payload was
  removed.
* Item and block textures are the ones supplied by the author (`time_wand`, `time_layer`); the
  placeholder generators keep existing files instead of overwriting them.

## 1.2.0

* New item `time_wand` (时间加速手杖): sneak + right click accelerates block entities or
  randomly ticking blocks from 4x up to 256x, 60 s per level, shown as a floating label plus
  particles on all six faces (see above for the mechanics).
* `magic_poison_bottle` recipe changed to the 3x3 layout from the requirements
  (redstone / 2 glowstone dust / glass bottle / fermented spider eye).
* Fixed the flight potion recipe showing "不可合成的药水": water bottles are now matched with
  the `neoforge:components` ingredient type, which keeps the `potion_contents` component.

## 1.1.1

* Fixed "the bottle cannot be worn / no charm slot in the curio GUI": Curios derives the slots a
  player owns from `data/<namespace>/curios/entities/*.json`, and the Curios jar itself ships no
  entity mapping at all (the `curios:charm` **item** tag only says *what* fits, not that the player
  *has* a charm slot). The mod now ships `data/curios/curios/entities/player.json` granting
  `minecraft:player` one `charm` slot, so the bottle both shows "可佩戴在：护符" and can be equipped.
* Both omni items now override `isCorrectToolForDrops` (always true, netherite tier) and
  `getDestroySpeed` (falls back to the configured 9.0 speed when the `tool` component is missing),
  so mining never depends on the component having been applied yet.
* Both omni items expose their combat/utility bonuses as vanilla item attribute modifiers, so the
  stock tooltip ("When in Main Hand:" / "When held:") lists them.

## 1.1.0

* New item `magic_poison_bottle` (魔法药水瓶): marks the effects the player currently has with
  `sneak + right click`, lists them in the tooltip and grants them with an infinite duration
  while worn in a Curios **charm** slot.
* Curios API is now a required dependency (`[9.0.0,)`), declared in `neoforge.mods.toml`, and
  the bottle is added to the `curios:charm` item tag.
* New potion type `xiabis_modpack_toolbox:flight` and the Apotheosis-gated recipe for a 24 hour
  flight potion, reusing `apothic_attributes:flying` instead of registering a second flight
  effect (the JSON `custom_effects` + `item_name` variant was dropped: item components in recipe
  JSON reject translatable names).

## 1.0.3

* Digging time per block was 18 ticks (0.9 s) instead of the instant 0.4 tick target
  from 1.0.2.

## 1.0.2

* Both items are netherite tier (the `incorrect_for_netherite_tool` tag is empty, i.e. every
  block that needs a proper tool can be harvested) and share a single digging speed:
  `PlayerEvent.BreakSpeed` replaces the vanilla speed with `hardness * 30 / 18`, which is
  the shared number of ticks per block regardless of the block hardness.
* The omni weapon now has a `minecraft:tool` component too (backfilled for weapons that were
  created before this version), so it can mine and harvest blocks.

## 1.0.1

* The weapon now extends `MaceItem`, so the fall smash reuses the vanilla mace feedback
  (impact sound, heavy/air variants, knockback wave, fall damage immunity window) instead of
  a hand-written copy that never ran (`Item#hurtEnemy` returned `false`, so `postHurtEnemy`
  was skipped).
* Multishot fires three arrows with the vanilla crossbow spread
  (`EnchantmentHelper#processProjectileSpread`), consuming a single arrow.
* Quick charge shortens the draw (`20 - 5 * level` ticks, minimum 3) and the client side
  `pull` / `pulling` model predicates read the enchantment straight off the stack, so the
  draw animation follows the shortened charge.
* New 4-stage draw textures for the weapon (`omni_weapon_pulling_0..3`).
* The sonic boom no longer ignores the shield mode: `LivingIncomingDamageEvent` cancels it
  while the tool is held up in shield mode.
* Staff mode casts the Iron's Spells spell selected in the spell book (reflection, optional).

## 1.0.0

- Initial release with the Omni Tool and Omni Weapon.
