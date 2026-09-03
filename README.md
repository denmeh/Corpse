# Corpse

[![](https://jitpack.io/v/denmeh/Corpse.svg)](https://jitpack.io/#denmeh/Corpse)
[![bStats Servers](https://img.shields.io/bstats/servers/33830?label=servers)](https://bstats.org/plugin/bukkit/Corpse/33830)
[![bStats Players](https://img.shields.io/bstats/players/33830?label=players)](https://bstats.org/plugin/bukkit/Corpse/33830)

Dead bodies in Minecraft for 1.8–26.2 servers.


## Installation

1. Install [PacketEvents](https://www.spigotmc.org/resources/packetevents-api.80279/) **2.13.0 or newer** (required for Minecraft 26.2).
2. Drop this jar into `plugins`.
3. Restart the server (do not use `/reload`). Use `/corpse reload` afterwards to apply config/message changes.


## Commands

| Command | Permission | Description |
|--------|------------|-------------|
| `/corpse spawn [player]` | `corpses.spawn` | Spawns your corpse at your feet, or another player’s if you give a name. |
| `/corpse remove <radius>` | `corpses.remove` | Removes all corpses within the given radius. |
| `/corpse reload` | `corpses.reload` | Reloads `config.yml` and `messages.yml`. Works from console. |

## Config (`config.yml`)

- `corpse-time` – Seconds before a corpse despawns (`-1` = never). Leftover loot is dropped on the ground.
- `on-death` – Spawn a corpse when a player dies.
- `show-tags` – Show name tags above corpses.
- `render-armor` – Render armor/items on the corpse.
- `corpse-distance` – Max blocks at which corpses are visible.
- `lootable-corpses` – Right-click to open inventory and loot.
- `despawn-when-empty` – Despawn a lootable corpse when its inventory is empty.
- `loot-click-range` – How close a ground click must be to open loot.
- `worlds` – World names where corpses spawn on death (empty = all). Commands still work everywhere.
- `entity-pose` – Pose used for the corpse entity (`SLEEPING` or `SWIMMING`).

Existing `config.yml` / `messages.yml` files are not overwritten. New keys use built-in defaults until you add them yourself.

## Messages (`messages.yml`)

All command and loot text is configurable. Use `&` color codes. `{amount}` is replaced in `corpses-deleted`.

## API

Add the dependency (e.g. JitPack) and use the **builder API** via `Corpse.fromPlayer()` or `Corpse.fromLocation()`:

```java
import com.github.denmeh.corpse.corpse.Corpse;
import com.github.denmeh.corpse.model.CorpseArmor;

// At player location, with their skin and armor
Corpse corpse = Corpse.fromPlayer(player).spawn();

// At a specific location
Corpse corpse = Corpse.fromPlayer(player).location(location).spawn();
Corpse corpse = Corpse.fromLocation(location).name(offlinePlayer.getName()).spawn();

// Custom armor (use CorpseArmor)
CorpseArmor armor = new CorpseArmor().boots(boots).leggings(leggings).chestplate(chestplate).helmet(helmet);
Corpse corpse = Corpse.fromPlayer(player).location(location).armor(armor).spawn();
Corpse corpse = Corpse.fromLocation(location).name(name).armor(armor).spawn();

// Remove a corpse
corpse.destroy();
```

The builder also supports `.textures(List<TextureProperty>)` for custom skins. Listen for right-clicks and attacks on corpses via `AsyncCorpseInteractEvent`; use `getAction()` to distinguish interact vs attack.

---

[![bStats](https://bstats.org/signatures/bukkit/Corpse.svg)](https://bstats.org/plugin/bukkit/Corpse/33830)
