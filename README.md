# compactspawners

> Fabric/Quilt mod, that adds automatic killing and drop storing in spawners

This mod changes the behaviour of the spawner to a fully automated killing machine. This way mobs don't spawn in the
world the drops and experience are stored in the spawner.

# Deprecation
This project is archived from 15/04/2024 on. This is caused by the bad implementation of this project and my lack of time. 
It is possible I revive this project at a later time. Feel free to fork or reimplement this project.

## Interactions

- Hitting or clicking on the spawner will transfer the experience to your character.
- Clicking on the spawner will open the drop menu.
- Clicking on a spawner with a spawner will add more spawners to the hosting spawner. This will increase the kill rate.
- A hopper below the spawner will suck up all mob drops.

## Config

The mod can be configured via the mod file, which is stored in `.minecraft/config/compactspawners.json`, or via the 
configuration menu in ModMenu.
<details>
<summary>compactspawners.json</summary>

```json
{
  "maxMergedSpawners": -1, // defines the number of spawners that can be merged into one
  "maxStoredExp": -1, // the maximum amount of experience stored in a hosting spawner
  "silkBreakSpawners": true,  // decides whether you can break a spawner with silk touch
  "requiredPlayerDistance": 32,  // the minimum player distance for the spawner to work
  "mobsPerSpawner": 4,  // defines how many mobs should spawn per spawner per period
  "luck": 1.0 // defines with what value of luck an entity should be killed inside a spawner
}
```
</details>

## Recipes
As long as Minecraft doesn't intend to add nbt crafting, there will be no recipes provided by this mod. I hope to be able to add 
configurable spawner recipes in the future.

## Other

⚠️ The development version is always the latest stable release of Minecraft.
Therefore, new features will only be available for the current and following Minecraft versions.

If you need help with any of my mods just join my [discord server](https://nyon.dev/discord)
