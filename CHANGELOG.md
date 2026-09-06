# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.1.0] - 2026-09-06

### Added
- `corpse-time-groups` – permission-based corpse lifetime. The **dead player’s** (skin owner’s) first matching permission sets how long that corpse lasts for everyone. Viewers all see the same body. Default `corpse-time` is unchanged.
- `/corpse spawn` uses the skin owner’s time group (offline names fall back to `corpse-time`).
- Builder API: `Corpse.fromPlayer(player).time(seconds)`.

## [4.0.0] - 2026-09-03

Dead bodies for Minecraft **1.8–26.2**.

### Breaking
- `/spawncorpse` and `/removecorpse` are now `/corpse spawn` and `/corpse remove`.
- Java packages moved from `com.github.unldenis.corpse` to `com.github.denmeh.corpse`.
- PacketEvents is no longer shaded. Install [PacketEvents 2.13.0+](https://www.spigotmc.org/resources/packetevents-api.80279/) or the plugin will not enable.

### Added
- `/corpse reload` – reloads `config.yml` and `messages.yml` (works from console).
- `despawn-when-empty` – lootable corpses despawn when the inventory is empty (default `true`).
- `loot-click-range` – right-click the ground under a sleeping corpse to loot it.
- `worlds` – optional whitelist for death corpses.
- bStats metrics ([plugin page](https://bstats.org/plugin/bukkit/Corpse/33830)).

### Fixed
- Loot GUI can be opened more than once; leftover items drop when the corpse despawns.
- Corpses no longer stay in the tab list or block reconnects.

## [3.0.0] - 2026-01-30

### Added
- CorpseBuilder API (`Corpse.fromPlayer()` / `Corpse.fromLocation()`). ([#22](https://github.com/denmeh/Corpse/pull/22))
- Configurable `entity-pose` and `CorpseArmor` class. ([#23](https://github.com/denmeh/Corpse/pull/23))
- Configurable messages (`messages.yml`).

### Changed
- Author / packages documented as `denmeh` instead of `unldenis`.

## [2.1.1] - 2026-01-30

### Added
- Minecraft 1.21.11 support. ([#21](https://github.com/denmeh/Corpse/pull/21))
- GitHub Actions workflow to publish the plugin jar.

## [2.1.0-rc.2] - 2025-12-05

### Fixed
- Maven build failure.

## [2.1.0-rc.1] - 2025-12-04

### Added
- Minecraft 1.21.8–1.21.10 support (not fully tested).

### Changed
- PacketEvents dependency updated to `2.10.1`.

### Fixed
- Build against `spigot-api` instead of `spigot` so JitPack/local Maven resolve correctly. ([#17](https://github.com/denmeh/Corpse/pull/17) by [@Despical](https://github.com/Despical))

## [2.0.0] - 2025-06-08

### Added
- Lootable corpses.
- `AsyncCorpseInteractEvent`.
- Minecraft 1.21.x support.

### Changed
- ProtocolLib replaced with PacketEvents.

### Fixed
- Multiple corpses with the same name when name tags are shown.

## [1.0.10] - 2024-01-14

### Added
- Minecraft 1.19.x and 1.20.x support, including 1.20.2. ([#8](https://github.com/denmeh/Corpse/pull/8) by [@lenlino](https://github.com/lenlino))

## [1.0.9] - 2022-08-24

### Changed
- Code style follows Google Java format.

### Fixed
- Player death message disappearing.

## [1.0.8] - 2022-06-24

### Fixed
- Console warn spam when changing world.

## [1.0.7] - 2022-06-20

### Added
- Minecraft 1.19 support.

## [1.0.6] - 2022-02-08

### Fixed
- `Could not find packet for type BED` on newer versions.

## [1.0.5] - 2022-02-08

### Added
- Minecraft 1.8–1.12 compatibility. ([#2](https://github.com/denmeh/Corpse/pull/2) by [@Happy-FZM](https://github.com/Happy-FZM))

## [1.0.3] - 2022-02-07

### Fixed
- `/spawncorpse` spawning an online player.
- Skin rendering on 1.8–1.12.
- 1.16.5 compatibility.

## [1.0.1] - 2022-01-24

### Added
- Command permissions.
- Public API improvements.

### Fixed
- Two corpses with the same name when `show-tags` is enabled.

## [1.0.0] - 2022-01-21

Initial release: packet-based player corpses.

[4.1.0]: https://github.com/denmeh/Corpse/compare/4.0.0...HEAD
[4.0.0]: https://github.com/denmeh/Corpse/compare/3.0.0...4.0.0
[3.0.0]: https://github.com/denmeh/Corpse/compare/2.1.1...3.0.0
[2.1.1]: https://github.com/denmeh/Corpse/compare/2.1.0-rc.2...2.1.1
[2.1.0-rc.2]: https://github.com/denmeh/Corpse/compare/2.1.0-rc.1...2.1.0-rc.2
[2.1.0-rc.1]: https://github.com/denmeh/Corpse/compare/2.0.0...2.1.0-rc.1
[2.0.0]: https://github.com/denmeh/Corpse/compare/1.0.10...2.0.0
[1.0.10]: https://github.com/denmeh/Corpse/compare/1.0.9...1.0.10
[1.0.9]: https://github.com/denmeh/Corpse/compare/1.0.8...1.0.9
[1.0.8]: https://github.com/denmeh/Corpse/compare/1.0.7...1.0.8
[1.0.7]: https://github.com/denmeh/Corpse/compare/1.0.6...1.0.7
[1.0.6]: https://github.com/denmeh/Corpse/compare/1.0.5...1.0.6
[1.0.5]: https://github.com/denmeh/Corpse/compare/1.0.3...1.0.5
[1.0.3]: https://github.com/denmeh/Corpse/compare/1.0.1...1.0.3
[1.0.1]: https://github.com/denmeh/Corpse/compare/e6bcc1f...1.0.1
[1.0.0]: https://github.com/denmeh/Corpse/commit/e6bcc1f
