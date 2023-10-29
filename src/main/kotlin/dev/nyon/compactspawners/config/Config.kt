package dev.nyon.compactspawners.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// version 1
@Serializable
data class Config(
    var maxMergedSpawners: Int = 4,
    var maxStoredExp: Int = 0,
    var silkBreakSpawners: Boolean = true,
    var requiredPlayerDistance: Int = 32,
    var mobsPerSpawner: Int = 4
)

var config = Config()

internal fun migrate(jsonTree: JsonElement, version: Int?): Config? {
    val jsonObject = jsonTree.jsonObject
    return when (version) {
        null -> Config(
            jsonObject["maxMergedSpawners"]?.jsonPrimitive?.content?.toIntOrNull() ?: return null,
            jsonObject["maxStoredExp"]?.jsonPrimitive?.content?.toIntOrNull() ?: return null,
            jsonObject["silkBreakSpawners"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: return null,
            jsonObject["requiredPlayerDistance"]?.jsonPrimitive?.content?.toIntOrNull() ?: return null,
            jsonObject["mobsPerSpawner"]?.jsonPrimitive?.content?.toIntOrNull() ?: return null,
        )

        else -> null
    }
}