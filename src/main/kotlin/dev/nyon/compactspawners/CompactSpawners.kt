package dev.nyon.compactspawners

import dev.nyon.compactspawners.config.loadConfig
import dev.nyon.compactspawners.config.saveConfig
import dev.nyon.compactspawners.spawner.CompactSpawnerEntity
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntityType


val json = Json {
    prettyPrint = true
    encodeDefaults = true
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object CompactSpawners : ModInitializer {
    private const val MOD_ID = "compactspawners"

    val blockEntityType: BlockEntityType<CompactSpawnerEntity> = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        ResourceLocation(MOD_ID, "spawner"),
        BlockEntityType.Builder.of({ pos, state -> CompactSpawnerEntity(pos, state) }, Blocks.SPAWNER).build(null)
    )

    override fun onInitialize() {
        loadConfig()
    }

    fun shutdown() {
        saveConfig()
    }
}