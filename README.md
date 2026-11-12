# FortniteToken

Paper plugin for Minecraft/Paper 26.3.

## Features
- Checks naturally generated dungeon chests using the vanilla loot table `minecraft:chests/simple_dungeon`.
- A 1-in-1000 chance when the dungeon chest is opened.
- On a successful roll, gives the player a sunflower named **Token**.
- Plays a short rare-drop animation with particles, sounds, and a chat announcement.
- Ordinary player-placed chests and other structure chests are ignored.

## Build
Requirements: JDK 25 and Gradle.

```bash
gradle build
```

The plugin JAR is created in `build/libs/FortniteToken-1.0.0.jar`.

Copy the JAR into the server's `plugins` folder and restart Paper 26.3.

## Detection note
A chest is accepted only while its vanilla dungeon loot table is attached. The roll is therefore performed on the chest's first loot-table-backed opening.
