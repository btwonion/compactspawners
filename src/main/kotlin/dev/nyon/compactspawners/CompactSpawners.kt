package dev.nyon.compactspawners

import dev.nyon.compactspawners.config.Config
import dev.nyon.compactspawners.config.migrate
import dev.nyon.compactspawners.spawner.CompactSpawnerEntity
import dev.nyon.konfig.config.config
import dev.nyon.konfig.config.loadConfig
import dev.nyon.konfig.config.saveConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntityType
import dev.nyon.compactspawners.config.config as internalConfig

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object CompactSpawners : ModInitializer {
    private const val MOD_ID = "compactspawners"

    val blockEntityType: BlockEntityType<CompactSpawnerEntity> =
        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation(MOD_ID, "spawner"),
            BlockEntityType.Builder.of({ pos, state -> CompactSpawnerEntity(pos, state) }, Blocks.SPAWNER).build(null)
        )

    override fun onInitialize() {
        config(
            FabricLoader.getInstance().configDir.resolve("$MOD_ID.json"),
            2,
            Config(),
            {}
        ) { jsonTree, version -> migrate(jsonTree, version) }
        internalConfig = loadConfig<Config>()
    }

    fun shutdown() {
        saveConfig(internalConfig)
    }
}
