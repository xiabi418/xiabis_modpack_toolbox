# xiabi's modpack toolbox

[简体中文](README_zh.md) | English

A collection of deliberately powerful tools, weapons, armor and machines for skipping frustrating modpack progression bottlenecks. Some features may significantly change pack balance.

**For players:** Minecraft 1.21.1 with NeoForge 21.1.x. Curios is required. Iron's Spellbooks, Apotheosis, GregTech Modern, Applied Energistics 2, Sophisticated Backpacks Sophisticated Storage and Mystical Agriculture add optional integrations or recipes.

## Omni Tool

One item combines a pickaxe, shovel, axe, hoe, shears, shield, wrench and staff.

- Choose **Omni (all)** or a single function from the item configuration screen. The default key is **V** and can be changed in the controls menu.
- Switch between Fortune 15 and Silk Touch I.
- Breaks blocks in about **0.3 seconds** and can harvest blocks that normally require a Netherite-tier tool.
- Main-hand use adds **5 blocks** of block reach. With Iron's Spellbooks installed, holding it in either hand also grants **20 spell power**, **5000 max mana**, **+500% cooldown speed** and **500% mana regeneration**.
- Grass is flattened with a regular right-click; sneak-right-click tills. Logs can be stripped, pumpkins carved, and shield mode blocks attacks. Optional wrench and spellbook integrations use compatible items from the installed pack.

## Omni Weapon

A single weapon combines a sword's sweep, an axe's shield-breaking, a mace's falling smash and bow-style ranged attacks.

- Right-click to draw and fire arrows; Multishot, Quick Charge and other supported weapon enchantments apply.
- Falling attacks use the familiar mace impact effects.
- Main-hand use adds **4 blocks** of entity reach.
- Unbreakable by default, with Looting 15.

## Omni Armor

A powerful helmet for either the vanilla head slot or the Curios head slot.

- Grants **60 armor** and **60 armor toughness** where the pack's attribute limits allow them.
- Provides strong protection, underwater breathing and immunity to nearly all damage sources other than `/kill`.
- Removes fog and darkness effects while worn.
- Open its block-highlighting screen with the configurable armor-menu key (default **H**). Marked blocks are outlined nearby; the screen does not consume the blocks you select.

## Magic Poison Bottle

A Curios charm that stores active potion effects.

- Sneak-right-click while you have effects to mark them in the bottle. Successfully marked effects are removed from you; an effect already stored at the same or a higher level is left alone.
- The bottle displays stored effects in its tooltip. Wearing it in a Curios **charm** slot grants them indefinitely. Removing the bottle removes the effects it granted.
- Crafting: redstone above a middle row of glowstone dust, glass bottle, glowstone dust; fermented spider eye below.

## Time Acceleration Wand

Sneak-right-click a ticking block, such as a furnace, or a randomly ticking block, such as a crop.

- Each click increases its speed through **4x, 8x, 16x, 64x, 128x and 256x** and resets its duration to **60 seconds**. The maximum level cannot be increased further.
- An expanded shell, floating multiplier/countdown and sparks mark accelerated blocks. Acceleration data is saved with the world.
- Crafting pattern:
  ```
  . Amethyst Block  Amethyst Shard
  . Stick           Amethyst Block
  Stick .           .
  ```

## Omni Generator

A fuel-powered generator with a compact control panel and configurable sides.

- Produces **4096 FE per tick** by default and stores **10,240 FE**. Coal, charcoal and lava are supported fuels.
- Its nine fuel slots hold large stacks; the lava tank stores up to **1024 buckets**.
- Choose FE, EU or AE output when the corresponding energy mod is installed. Conversion rates are **1 FE = 0.25 EU = 0.5 AE**.
- Configure each side for fluid input, item input, energy output or combined input/output. Active input and output can be toggled from the screen.
- **Energy Cards** increase storage; **Speed Cards** increase generation. Both scale with the number installed.

## Giant Battery Box

A large FE buffer with no screen. Right-click a side to switch it between input and output; all sides start as input. Sneak-right-click toggles automatic input/output. The action bar reports each change. Stored energy is retained when the block is picked up or broken.

## Omni Greenhouse

An automated farm for crops and saplings, available in the toolbox creative tab.

- Insert one seed or sapling, water and energy. The seed is not consumed; each harvest uses **1000 mB of water** and **1000 FE**.
- Crops are harvested from their mature drops, and saplings produce their matching logs and sticks. Mystical Agriculture seeds can produce their crop's usual drops, including essence.
- Nine output slots, a water tank, an energy buffer and slots for **Speed Cards** and **Energy Cards**. The machine pauses when all output slots are full.
- Configure sides for water input, energy input or item output. Cards shorten the harvest cycle or increase the energy buffer.

## Storage Upgrades

When the corresponding Sophisticated mod is installed, surround a Diamond Backpack or Diamond Barrel with eight Diamond Blocks to upgrade it to Netherite tier.

- The upgraded backpack has **15 upgrade slots** and **4096 item slots**.
- The upgraded barrel has **15 upgrade slots** and **8192 item slots**. Both upgrades keep their contents, and the barrel keeps its wood style.
- The upgraded containers appear in the toolbox creative tab when their mod is installed.

## Potion of Flight

When Apotheosis and Apothic Attributes are installed, three Water Bottles, a Phantom Membrane, a Fermented Spider Eye and a Popped Chorus Fruit craft a **24-hour Flight Potion**. It uses the flight effect supplied by Apothic Attributes.

## Advanced Totem of Undying

A durable totem that prevents death like the vanilla totem. Each rescue consumes one durability point, and it breaks after **256 uses**.

- On rescue, it grants the vanilla effects plus **Strength V**, **Speed II** and **Jump Boost III** for **30 seconds**, and **Water Breathing** for **3 minutes**.
- Crafting pattern:
  ```
  Redstone  Quartz        Redstone
  Obsidian  Totem         Obsidian
  Emerald Block  Gold Ingot  Emerald Block
  ```

## Omni Fluid Tank

The **Omni Fluid Tank** (`omni_fluid_tank`) accepts any number of different fluid types, with **8 buckets per type**.

- A fluid that reaches 8 buckets can be extracted indefinitely without reducing the stored amount.
- Additional input of that same full fluid is voided; other fluids remain independent.
- Breaking the tank preserves all stored fluids in the dropped item, and placing it restores them.
- The item tooltip lists one line per stored fluid: full types show `无限`, while partial types show their amount as `xxmb/8000mb`.
- Crafting pattern:
  ```
  Copper Ingot  Copper Ingot  Copper Ingot
  Copper Block  Any Glass     Copper Block
  Copper Ingot  Copper Ingot  Copper Ingot
  ```

The Simplified Chinese locale is stored as readable UTF-8 text, and the advanced totem no longer has a tooltip.

**2.4.2** fixes startup on installations without Applied Energistics 2.

## Polished Concrete

All 32 polished and glowing concrete variants are grouped in the dedicated **xiabi's Modpack Toolbox - Building Materials** creative tab. Its icon is Glowing White Polished Concrete.


The mod adds 16 color variants of **Polished Concrete** and matching **Glowing Polished Concrete** blocks.

- A stonecutter turns one ordinary concrete into one polished block. Fill the crafting grid with nine ordinary concrete blocks of one color to craft nine polished blocks.
- Surround a polished block with cobblestone to duplicate nine blocks of that color.
- Add glowstone dust to make the glowing variant; surround a glowing block with cobblestone to duplicate nine glowing blocks.
- Glowing blocks emit light level 15. Fusion 1.3.16 or later is required on the Minecraft 1.21.1 NeoForge client. Connected textures join adjacent blocks of the same color and variant.

## Dimension Teleport Blocks

Craft the entry blocks from a full 3×3 grid of dirt (private Overworld), netherrack (private Nether), or end stone (private End). Right-click an entry block to travel to the same X/Z location in its private dimension. The block searches nearby for a safe surface landing and creates an owner-bound return block next to it. Right-click that return block to go back to the exact origin dimension, position, and rotation; it is consumed on successful return. Return blocks have no recipe and cannot be mined.

## Private Dimensions

The mod adds three seed-aligned dimensions:

- `xiabis_own_dim_overworld`: vanilla Overworld biomes and decoration with a flat stone foundation through Y=63, a grass surface at Y=63, and no cave carving.
- `xiabis_own_dim_nether`: vanilla Nether biome and decoration pipeline, solid netherrack through Y=128, and no generated upper bedrock ceiling.
- `xiabis_own_dim_end`: vanilla End biomes and decoration, no main-island terrain, solid end stone through Y=128, and a bedrock floor at Y=0.

Biome/tag-based ores, features, and structures from other mods can follow these dimensions because they reuse the vanilla biome sources and decoration pipeline. Mods that hard-code only the original Overworld, Nether, or End dimension IDs require their own compatibility support.


When Applied Energistics 2 is installed, the **Compressed Matter Condenser** (`compressed_condenser`) connects to an ME network and converts selected network items into AE2 matter balls or singularities.

- Insert an AE2 storage cell and mark up to 27 item filters. The machine only extracts marked items while it is connected to an online ME network.
- The base processing rate is **102,400 items per tick**. The condenser follows AE2’s configured costs. On the tested default settings, one matter ball costs **256 items** and one singularity costs **256,000 items**.
- Six card slots accept one Inverter Card, one Redstone Card, one Fuzzy Card and up to five Speed Cards. Each Speed Card adds ten times the base rate.
- Enable network output to send finished products back into the ME network. Otherwise products are written to the inserted storage cell. The inserted cell is always required.
- The GUI provides filter slots, card slots, a storage-cell slot, mode controls and the current aggregation value.
- The crafting recipe is only available when AE2 is installed.


See [CHANGELOG.md](CHANGELOG.md) for the version history. For mod issues, use the [GitHub issue tracker](https://github.com/xiabi418/xiabis_modpack_toolbox/issues).
