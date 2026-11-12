# FortniteToken

Paper plugin for Minecraft/Paper 26.3.

## Features
- Checks naturally generated loot containers in loaded chunks, not just dungeon chests.
- Works with naturally generated containers that have a vanilla loot table (including different structure chest loot tables).
- Configurable 1-in-N chance when a marked natural loot container is opened.
- Configurable token material, display name, and lore.
- Configurable announcement, particle effects, sound effects, and animation duration.
- Gives the player a token with a plugin-specific persistent marker.
- If the inventory is full, the Token drops naturally at the player's feet.
- Test command: `/fttoken test` gives a Token and plays the same animation.
- Reload command: `/fttoken reload` reloads `plugins/FortniteToken/config.yml` without restarting the server.

## Configuration

The default `config.yml` is generated automatically on the first start. Example:

```yaml
drop-chance:
  denominator: 1000

token:
  material: SUNFLOWER
  display-name: "&6Token"
  lore:
    - "&7Ein seltener Fortnite-Token"
    - "&8Gefunden in einer natürlich generierten Truhe"

animation:
  announcement:
    enabled: true
    message: "&6✦ &eULTRA RARER TOKEN GEFUNDEN! &6✦"
  particles:
    enabled: true
  sounds:
    enabled: true
  duration-ticks: 40
```

- `denominator: 1000` means 1 chance in 1000; `500` means 1 in 500; `1` means every eligible opening.
- `token.material` accepts a valid Bukkit material such as `SUNFLOWER`, `EMERALD`, or `DIAMOND`.
- Use `&` color codes in `display-name`, `lore`, and the announcement.
- Animation duration is in ticks (20 ticks = 1 second; maximum 200).
- Run `/fttoken reload` after editing the config.

## Permissions
- `fortnitetoken.test`: allows `/fttoken test` (default: operators).
- `fortnitetoken.admin`: allows `/fttoken reload` (default: operators).

## Build
Requirements: JDK 25 and Gradle.

```bash
gradle build
```

The plugin JAR is created in `build/libs/FortniteToken-1.2.0.jar`.

Copy the JAR into the server's `plugins` folder and restart Paper 26.3.

## Detection details and limitation
On chunk load, the plugin marks tile entities that still have a vanilla loot table. This preserves the natural-container marker after Minecraft consumes the loot table on first opening. Player-placed containers do not have a loot table when placed and are not marked.

As with any Bukkit/Paper plugin, verify behavior on the exact server build.
