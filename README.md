# FortniteToken

Paper plugin for Minecraft/Paper 26.3.

## Features
- Checks naturally generated loot containers in loaded chunks, not just dungeon chests.
- Works with naturally generated containers that have a vanilla loot table (including different structure chest loot tables).
- A 1-in-1000 chance each time a marked natural loot container is opened.
- Gives the player a sunflower named **Token** with a plugin-specific persistent marker.
- Plays a short rare-drop animation with particles, sounds, and a chat announcement.
- If the inventory is full, the Token drops naturally at the player's feet.
- Test command: `/fttoken test` gives a Token and plays the same animation without requiring a chest.

## Test command permission
- Permission: `fortnitetoken.test`
- Default: operators (`op`)
- Grant explicitly with: `/lp user <player> permission set fortnitetoken.test true` (LuckPerms)
- Run: `/fttoken test`

## Build
Requirements: JDK 25 and Gradle.

```bash
gradle build
```

The plugin JAR is created in `build/libs/FortniteToken-1.1.0.jar`.

Copy the JAR into the server's `plugins` folder and restart Paper 26.3.

## Detection details and limitation
On chunk load, the plugin marks tile entities that still have a vanilla loot table. This preserves the natural-container marker after Minecraft consumes the loot table on first opening. Player-placed containers do not have a loot table when placed and are not marked.

As with any Bukkit/Paper plugin, the final behavior should be verified on the exact server build. The included test command independently verifies reward delivery and animation.
