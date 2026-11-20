# xiabi's modpack toolbox

Minecraft **1.21.1** / **NeoForge 21.1.x** mod for modpack play. Adds two unbreakable,
non-stackable, highly enchanted "everything" items plus a Curios charm that stores buffs:

* `xiabis_modpack_toolbox:omni_tool` — 通用多功能工具 / Omni Tool
* `xiabis_modpack_toolbox:omni_weapon` — 通用多功能武器 / Omni Weapon
* `xiabis_modpack_toolbox:magic_poison_bottle` — 魔法药水瓶 / Magic Poison Bottle
* `xiabis_modpack_toolbox:time_wand` — 时间加速手杖 / Time Acceleration Wand
* `xiabis_modpack_toolbox:flight` — 飞行药水（24 小时） / Potion of Flight (24 hours)

**Dependencies:** Curios API is required (`[9.0.0,)`, tested with 9.5.1+1.21.1). Apotheosis
(and therefore Apothic Attributes) is optional; the 24 hour flight potion recipe only loads
when it is installed.

## Features

### Omni Tool
* Works as pickaxe, shovel, axe, hoe, shears, shield, wrench and staff.
* Default state mines/tills with everything enabled:
  * plain right click on grass = shovel flatten,
  * sneak + right click = hoe till,
  * right click on logs = axe strip, on pumpkins = shears carve,
  * right click with a wrench selected = wrench action (proxy from `c:tools/wrench`,
    otherwise block state rotation),
  * sneak + right click in the air = staff cast (proxy spell item) / shield block otherwise.
* Menu key **V** opens the mode screen: pick a single tool function or "Omni (all)",
  and toggle **Silk Touch mode** (off = Fortune 15, on = Silk Touch I).
* 79 900 durability, unbreakable by default (`minecraft:unbreakable` component).
* Netherite tier and one shared digging speed: every block breaks in **6 ticks (0.3 s)**
  (`PlayerEvent.BreakSpeed` sets the speed to `hardness * 5`, so haste, water and
  airborne penalties cannot change the time).
* Held in the **main hand** it adds **+5 block interaction range**.
* With Iron's Spellbooks installed, holding it in the main *or* off hand adds
  **+20 spell power**, **+5000 max mana**, **+5 cooldown reduction** (the attribute value goes
  from 1.0 to 6.0, which the mod's soft cap turns into ~5% cooldown time) and
  **+5 mana regeneration** (1.0 -> 6.0, i.e. six times the regen rate).
  The attributes are resolved by reflection, so the mod still works without Iron's.
* Attack damage **4.18**, attack speed **418** (literal values from the spec).
* Accepts every enchantment (pickaxe/shovel/axe/hoe/shears/sword/bow/crossbow tables),
  including mutually exclusive ones (merged in the anvil).
* Apotheosis: loot category is overridden to `apotheosis:breaker` through a data map, and
  4 gem sockets are seeded so gems can be inserted.
* Recipe (shapeless): wooden pickaxe + wooden axe + shield + wooden hoe + wooden shovel +
  shears + 3 planks.

### Omni Weapon
* Mace fall attack (`getAttackDamageBonus` + ground smash / knockback), sword sweep,
  axe shield disabling, bow shooting (right click, consumes arrows from the inventory),
  crossbow enchantments.
* Attack damage = **number of installed mods** (1 mod = 1 damage point; the loader's
  built-in `minecraft` / `neoforge` entries are not counted),
  attack speed **418**.
* 32 500 durability, unbreakable, Looting 15 by default.
* Also netherite tier and digs every block in **6 ticks (0.3 s)**, same as the omni tool.
* Held in the **main hand** it adds **+4 entity interaction range**.
* Accepts every enchantment including conflicting ones; Apotheosis loot category
  `apotheosis:melee_weapon` + 4 sockets.
* Recipe (shapeless): wooden axe + wooden sword + bow + crossbow + 5 cobblestone.

### Magic Poison Bottle (Curios charm)
* `sneak + right click` with any active mob effect marks the effect (its current amplifier) into
  the bottle. Marking the same effect again only raises the level when the new level is higher;
  lower or equal levels are ignored. Up to 16 effects can be stored.
* The marked effects and their levels are listed in the tooltip below the item name.
* Wearing the bottle in a **Curios charm slot** grants every marked effect with an **infinite
  duration**; unequipping removes the effects it granted (only if they are still infinite and at
  the same amplifier).
* Recipe (shaped, 3x3):
  ```
  . R .
  G B G
  . F .
  ```
  `R` redstone, `G` glowstone dust, `B` glass bottle, `F` fermented spider eye.

### Time Acceleration Wand (时间加速手杖)
* `sneak + right click` a block entity (furnace, hopper, brewing stand, ...) or a block that
  accepts random ticks (crops, saplings, ...) to accelerate it.
* Every click steps the multiplier along **4x → 8x → 16x → 64x → 128x → 256x** and resets the
  timer to **60 s**; at 256x further clicks are refused until the timer runs out.
* Acceleration follows the "Time in a Bottle" / "Just Dire Things" approach: each game tick the
  block entity ticker (or `BlockState#randomTick`) runs `multiplier - 1` extra times, with a
  global budget of 2048 extra ticks per level tick so a few 256x blocks cannot stall the server.
* Accelerated blocks are wrapped in a glowing block shell: a `minecraft:block_display` renders
  the internal `time_layer` block scaled to 1.2 (so the cube sits 0.1 blocks outside every face,
  no z-fighting) and a `minecraft:text_display` above it shows `4x - 59s`, always facing the
  player. Both are plain vanilla entities created by the server, so no client code or custom
  packets are involved. `ELECTRIC_SPARK` particles on the six faces are still emitted as well.
* Recipe (shaped, 3x3):
  ```
  . A B
  . C A
  D . .
  ```
  `A` amethyst block (2x), `B` amethyst shard, `C`/`D` stick.
* State is persisted per dimension in `xmt_time_acceleration.dat`, so accelerations survive a
  restart and stop cleanly when the block is removed or the timer expires.

### Potion of Flight (24 hours)
* Registered as the potion type `xiabis_modpack_toolbox:flight`: one effect,
  `apothic_attributes:flying` (shipped by Apothic Attributes, so this mod does not register a
  duplicate flight effect), duration **1 728 000 ticks = 86 400 s = 24 h** (not drinkable-time
  limited, it counts down like any potion).
* Recipe (shapeless, only loaded when Apotheosis + Apothic Attributes are present):
  3 water bottles + phantom membrane + fermented spider eye + popped chorus fruit. The water
  bottles use the `neoforge:components` ingredient type; plain `{"item": "minecraft:potion",
  "components": {...}}` entries silently lose their components (the recipe would consume any
  potion and JEI showed "不可合成的药水").

### Models
Placeholder 16x16 textures are generated for every state. The omni tool has its own item
model per tool kind (`omni_tool_fortune_<kind>` / `omni_tool_silk_<kind>`) selected with
`custom_model_data`; the omni weapon has a single model.

| custom_model_data | state |
|---|---|
| 0 / 1 | Omni (all functions), fortune / silk |
| 11–18 | pickaxe … staff, fortune |
| 21–28 | pickaxe … staff, silk |

## Storage upgrade recipes (Sophisticated mods)

Two recipes are only loaded when their mod is installed.

**Sophisticated Backpacks** — 8 diamond blocks around a diamond backpack:

```
D D D
D B D      D = diamond block, B = diamond backpack
D D D
```

produces a netherite backpack named "[obfuscated]库存不足恐惧症[obfuscated]" (red, bold) with
15 upgrade slots and 4096 item slots.

**Sophisticated Storage** — 8 diamond blocks around a diamond barrel (any wood):

```
D D D
D B D      B = diamond barrel (the wood type is carried over)
D D D
```

produces the matching netherite barrel named "[obfuscated]容量不足恐惧症[obfuscated]" with
15 upgrade slots and 8192 item slots.

Both upgrades keep whatever was inside: the stored inventory is addressed by the
`sophisticatedcore:storage_uuid` component, which is copied onto the result, and the barrel's
`sophisticatedstorage:wood_type` is copied as well. Implemented by
`crafting/ContainerUpgradeRecipe` (a shaped recipe that post-processes its result), so recipe
viewers still render the pattern normally. Because the new container shares the storage UUID,
the original item must be consumed by the recipe — it is.

## Building

Requirements: **JDK 21** and network access (the build resolves NeoForge from
`neoforged.forgecdn.net` and downloads the Curios API jar from `cdn.modrinth.com`).

```bash
./gradlew build
```

The jar ends up in `build/libs/xiabis_modpack_toolbox-<version>.jar`.

```bash
./gradlew runServer   # dev server, --nogui; run/eula.txt is created on first launch
./gradlew runClient   # dev client
```

The Curios API jar is **not** committed (it is a third-party LGPL-3.0 binary). The
`downloadCurios` task fetches `curios-neoforge-9.5.1+1.21.1.jar` into `libs/` when it is
missing and wires it into the build; `libs/` is ignored by git.

## Development notes

`libs/curios-neoforge-9.5.1+1.21.1.jar` (downloaded by `./gradlew downloadCurios`) is wired in through `implementation files(...)`, which
gives the dev runs both the compile-time API and Curios as a runtime mod (ModDevGradle has no
`modRuntimeOnly` configuration; it passes runtime mods through `fml.modFolders` /
`additionalRuntimeClasspath`). To smoke test the Apotheosis integration, drop
`Placebo`, `ApothicAttributes` and `Apotheosis` jars into `libs/` and add the matching
`implementation files(...)` lines; they are not needed to compile the mod.

## Known limitations

* The tool does not shear sheep (shears behavior is limited to block mining and carving).
* Wrench integration is generic: the first item found in `c:tools/wrench` (or an item whose
  id/class contains `wrench` / `screwdriver`) is used as a proxy; if nothing is found the
  wrench falls back to block state rotation.
* Staff mode first tries Iron's Spells `Utils#serverSideInitiateCast` (reflectively, only when
  the mod is installed) so the spell selected in a spell book is cast with the normal mana /
  cooldown pipeline. Without that mod it falls back to borrowing a `c:tools/staff` /
  staff-like item and calling its `use`.
* Apotheosis socket seeding (4 sockets) uses reflection and is skipped when the mod is absent.
* `data/curios/curios/entities/player.json` grants players exactly one `charm` slot. If another
  datapack already defines a `charm` slot for players with a different size, that definition is
  overwritten (Curios merges entity slot maps by slot id) - delete this file from the jar in that
  case.
* The `data/apotheosis/.../loot_category_overrides.json` data map produces a harmless
  `non-existent data map type` warning when Apotheosis is not installed (NeoForge's data map
  loader ignores `neoforge:conditions` overrides).

## Omni Generator (`omni_generator`)

A block that burns solid fuel or lava and pushes the produced energy out as **FE**, **EU**
(GregTech Modern) or **AE** (Applied Energistics 2).

* Generation: **4096 FE/t** by default; the output rate equals the generation rate.
* Internal storage: **10 240 FE**, fuel: **9 slots x 1024 items** (coal / charcoal),
  fluid: **1024 B of lava** (1 B = 1000 mB = 20 000 burn ticks).
* Conversion: `1 FE = 0.5 AE = 0.25 EU` (4 FE per EU, 2 FE per AE).
* Every face is configured on its own by cycling through
  **fluid input -> item input -> energy output -> input + output** with left / right click.
* "Active output" pushes energy every tick to the faces configured as energy output;
  "active input" pulls items / lava out of the neighbouring inventories and tanks.
* GUI layout follows the requirements: the 3x3 button block in the upper left
  (`active output`, `top`, `active input` / `left`, `front`, `right` / `back`, `bottom`,
  `energy type`), the 9 fuel slots, a lava gauge, an energy gauge and the two card slots in the
  upper right.
* Upgrade cards (`energy_card` / `speed_card`, up to **64 of each**) scale the buffer and the
  generation **linearly**: capacity `10 240 x (1 + cache cards)` FE,
  generation `4096 x (1 + efficiency cards)` FE/t.
* Recipes (shaped):
  * generator: `furnace smoker furnace` / `iron_ingot copper_block iron_ingot` /
    `obsidian obsidian obsidian`
  * `energy_card`: `gold redstone gold` / `redstone amethyst_shard redstone` /
    `gold redstone gold`
  * `speed_card`: `gold glowstone gold` / `glowstone amethyst_shard glowstone` /
    `gold glowstone gold`
* Integration is reflective and optional: EU output needs GregTech Modern **1.21.1 (7.x)**
  (`IEnergyContainer#acceptEnergyFromNetwork`, ULV 8 EU amps); AE output injects into any
  neighbouring block entity that implements `IAEPowerStorage` (AE2's Energy Acceptor does),
  so the FE path keeps working even when neither mod is installed.

## Giant Battery Box (`omni_battery`)

An unbounded FE buffer without a GUI. Right click a face to flip it between **energy input** and
**energy output** (all six faces start as input); sneak + right click any face toggles **active
I/O**, which every tick pushes energy out of the output faces and pulls energy from the input
faces (off by default). Every interaction is reported on the action bar, e.g.
`Top: energy output, stored 1234567 FE`.

* Capacity: the largest value the FE API supports, `Integer.MAX_VALUE` (2 147 483 647 FE).
* Transfer (`receiveEnergy` / `extractEnergy` and the active I/O pass): up to
  **1 000 000 FE per tick per side**, so a full battery can be drained quickly.
* Faces only expose what they are configured for: input faces reject `extractEnergy`, output
  faces reject `receiveEnergy`, which means pipes and cables automatically respect the setting.
* Recipe (shaped):

  ```
  I C I      I = iron ingot, C = copper ingot
  B R B      B = iron block, R = redstone block
  B P B      P = copper block
  ```

The block id is lowercase (`omni_battery`); Minecraft resource locations cannot contain uppercase
letters, so the `omni_Battery` from the requirements is registered as `omni_battery`.

## Item tags

Both items are added to the vanilla and NeoForge common tool/weapon tags so modpacks and
other mods recognise them (`data/minecraft/tags/item/*` and `data/c/tags/item/*`):

* `omni_tool`: `minecraft:pickaxes`, `shovels`, `axes`, `hoes`, `swords`,
  `cluster_max_harvestables`, `breaks_decorated_pots`, `enchantable/{mining, mining_loot,
  durability, weapon, sharp_weapon, sword, fire_aspect, vanishing}`,
  `c:tools`, `c:tools/mining_tool`, `c:tools/melee_weapon`, `c:tools/shield`,
  `c:tools/shear`, `c:enchantables`.
* `omni_weapon`: `minecraft:axes`, `swords`, `cluster_max_harvestables`, `breaks_decorated_pots`,
  `enchantable/{mining, mining_loot, durability, weapon, sharp_weapon, sword, fire_aspect,
  vanishing, mace, bow, crossbow}`, `c:tools`, `c:tools/mining_tool`,
  `c:tools/melee_weapon`, `c:tools/ranged_weapon`, `c:tools/bow`, `c:tools/crossbow`,
  `c:tools/mace`, `c:enchantables`.

`c:tools/wrench` and `c:tools/staff` are intentionally **not** claimed: those interactions are
forwarded to a real wrench/staff item found at runtime, and tagging the toolbox items as
wrenches would make other mods (and the forwarding code) treat them as the genuine article.

## Changes in 1.3.8

* New block **Giant Battery Box** (`omni_battery`): unbounded FE storage, per-face input/output
  mode, active I/O toggle, action bar feedback, no GUI.
* The oversized Sophisticated Backpacks / Storage containers are now listed in this mod's creative
  tab when their mod is installed (with the obfuscated red names and the 4096/8192 slot counts).

## Changes in 1.3.4 - 1.3.7

* 1.3.4: the mods.toml is now maintained by the author (only `version` stays templated).
* 1.3.5: mod logo (`icon.png`), issue tracker URL and the provided card textures.
* 1.3.6: provided generator block textures, including a dedicated bottom face.
* 1.3.7: storage upgrade recipes for Sophisticated Backpacks / Storage (8 diamond blocks around a
  diamond container -> oversized netherite container that keeps its contents).

## Changes in 1.3.3

* The magic poison bottle now **consumes** an effect when marking it succeeds: the marked effect is
  removed from the player. Effects that are already marked at the same (or a higher) level, and
  instances weaker than the marked level, are left alone.
* Example: with Speed II and Jump Boost I you mark both, both are stored at level 2 / 1 and removed
  from you. Drinking Speed I later and marking again does nothing (already marked at a higher
  level), while Speed III raises the stored level to 3 and is consumed.

## Changes in 1.3.2

* Fixed the omni weapon providing mining: it no longer carries the omni tool's mining rules.
  The weapon keeps only the sword's block interaction (cobwebs at 15x, sword-efficient blocks at
  1.5x) and cannot harvest any block that needs a pickaxe/shovel/hoe/shears, so stone, ores and
  logs give no drops and break at normal hand speed.
* The custom 0.3 s break speed now only applies to the **omni tool**; the weapon is not affected.
* Weapon stacks from older versions are migrated on first tick (their old 8-rule mining `tool`
  component is replaced with the 2-rule sword component).
* The weapon was also removed from the `c:tools/mining_tool`, `minecraft:cluster_max_harvestables`,
  `minecraft:enchantable/mining` and `minecraft:enchantable/mining_loot` tags.

## Changes in 1.3.1

* Removed the custom tooltip lines from both omni items (the "Mining tier", "Mining speed" and
  reach lines are gone).
* Reach and Iron's Spellbooks bonuses are now **real item attribute modifiers** built in
  `getDefaultAttributeModifiers`, so the stock tooltip renders them under
  "When in Main Hand:" / "When held:" and no per-tick player patching is needed
  (`PlayerAttributesHandler` was deleted).
* Iron's Spellbooks defines spell power, cooldown reduction and mana regeneration as percentage
  attributes (base 1.0 = 100%), therefore the +20 spell power addition shows up as
  "+2000% Spell Power" (total 21.0) while max mana shows as a flat "+5000 Max Mana".

## Changes in 1.3.0

* New block `omni_generator` (万用发电机) with its GUI, face routing, fuel / lava burning,
  FE / EU / AE output and the two upgrade cards `energy_card` and `speed_card`
  (see the Omni Generator section above).

## Changes in 1.2.3

* Added the vanilla `minecraft:*` and NeoForge `c:*` item tags listed above so modpacks and
  other mods detect the omni tool / omni weapon as tools and weapons.

## Changes in 1.2.2

* Omni Tool: **+5 block interaction range** in the main hand; with Iron's Spellbooks also
  **+20 spell power**, **+5000 max mana**, **+500% spell cooldown speed** (attribute 1.0 -> 6.0)
  and **+500% mana regeneration** (1.0 -> 6.0) while held in either hand.
* Omni Weapon: **+4 entity interaction range** in the main hand.
* Digging time for both items changed from 18 ticks (0.9 s) to **6 ticks (0.3 s)** per block.
* Range and magic bonuses are item attribute modifiers now, so the vanilla tooltip shows them
  (the magic entries only appear when Iron's is loaded).

## Changes in 1.2.1

* The acceleration marker is now a real block shell plus a floating countdown text entity instead
  of a client side text renderer that did not show up in game: a `minecraft:block_display` shows
  the internal `time_layer` block scaled 1.2 (0.1 blocks outside the face, no z-fighting, full
  brightness) and a `minecraft:text_display` shows `4x - 59s`. The old client sync payload was
  removed.
* Item and block textures are the ones supplied by the author (`time_wand`, `time_layer`); the
  placeholder generators keep existing files instead of overwriting them.

## Changes in 1.2.0

* New item `time_wand` (时间加速手杖): sneak + right click accelerates block entities or
  randomly ticking blocks from 4x up to 256x, 60 s per level, shown as a floating label plus
  particles on all six faces (see above for the mechanics).
* `magic_poison_bottle` recipe changed to the 3x3 layout from the requirements
  (redstone / 2 glowstone dust / glass bottle / fermented spider eye).
* Fixed the flight potion recipe showing "不可合成的药水": water bottles are now matched with
  the `neoforge:components` ingredient type, which keeps the `potion_contents` component.

## Changes in 1.1.1

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

## Changes in 1.1.0

* New item `magic_poison_bottle` (魔法药水瓶): marks the effects the player currently has with
  `sneak + right click`, lists them in the tooltip and grants them with an infinite duration
  while worn in a Curios **charm** slot.
* Curios API is now a required dependency (`[9.0.0,)`), declared in `neoforge.mods.toml`, and
  the bottle is added to the `curios:charm` item tag.
* New potion type `xiabis_modpack_toolbox:flight` and the Apotheosis-gated recipe for a 24 hour
  flight potion, reusing `apothic_attributes:flying` instead of registering a second flight
  effect (the JSON `custom_effects` + `item_name` variant was dropped: item components in recipe
  JSON reject translatable names).

## Changes in 1.0.1

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

## Changes in 1.0.3

* Digging time per block was 18 ticks (0.9 s) instead of the instant 0.4 tick target
  from 1.0.2.

## Changes in 1.0.2

* Both items are netherite tier (the `incorrect_for_netherite_tool` tag is empty, i.e. every
  block that needs a proper tool can be harvested) and share a single digging speed:
  `PlayerEvent.BreakSpeed` replaces the vanilla speed with `hardness * 30 / 18`, which is
  the shared number of ticks per block regardless of the block hardness.
* The omni weapon now has a `minecraft:tool` component too (backfilled for weapons that were
  created before this version), so it can mine and harvest blocks.
