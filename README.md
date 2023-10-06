# compactspawners

> Fabric/Quilt mod, that adds automatic killing and drop storing in spawners

This mod changes the behaviour of the spawner to a fully automated killing machine. This way mobs don't spawn in the
world and the
spawn in the world and the drops and experience are stored in the spawner.

## Interactions

- Hitting or clicking the spawner will transfer the experience to your character.
- Clicking on the spawner will open the drop menu.
- Clicking on a spawner with a spawner will add more spawners to the hosting spawner. This will increase the kill rate.
- A hopper under the spawner will suck up all mob drops

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
  "mobsPerSpawner": 4  // defines how many mobs should spawn per spawner per period
}
```

</details>

## Other

⚠️ The development version is always the latest stable release of Minecraft.
Therefore, new features will only be available for the current and following Minecraft versions.

If you need help with any of my mods just join my [discord server](https://nyon.dev/discord)
