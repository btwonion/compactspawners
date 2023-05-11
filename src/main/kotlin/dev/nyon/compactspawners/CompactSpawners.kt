package dev.nyon.compactspawners

import dev.nyon.compactspawners.config.loadConfig
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer

val json = Json {
    prettyPrint = true
    encodeDefaults = true
}

@Suppress("unused")
class CompactSpawners : ModInitializer {
    override fun onInitialize() {
        loadConfig()
    }
}