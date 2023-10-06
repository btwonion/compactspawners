package dev.nyon.compactspawners.config

import dev.nyon.compactspawners.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.fabricmc.loader.api.FabricLoader
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
data class Config(
    var maxMergedSpawners: Int = -1,
    var maxStoredExp: Int = -1,
    var silkBreakSpawners: Boolean = true,
    var requiredPlayerDistance: Int = 32,
    var mobsPerSpawner: Int = 4
)

var config = Config()
private val path = FabricLoader.getInstance().configDir.toAbsolutePath().resolve("compactspawners.json")
    .also { if (!it.exists()) it.createFile() }

fun saveConfig() = path.writeText(json.encodeToString(config))

fun loadConfig() {
    if (path.readText().isEmpty()) {
        saveConfig()
        return
    }

    config = json.decodeFromString(path.readText())
}