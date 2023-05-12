package dev.nyon.compactspawners.storage

import dev.nyon.compactspawners.CompactSpawners
import dev.nyon.compactspawners.utils.toItemList
import dev.nyon.compactspawners.utils.toTagList
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData

class SpawnerSavedData(level: ServerLevel) : SavedData() {

    companion object {
        const val DATA_NAME = "${CompactSpawners.modID}_spawner_data"
    }

    init {
        level.dataStorage.computeIfAbsent({ load(it) }, { SpawnerSavedData(level) }, DATA_NAME)
    }

    var dimensionStorages = mutableListOf<DimensionStorage>()
    override fun save(compoundTag: CompoundTag): CompoundTag {
        dimensionStorages.forEach {
            val dimensionCompoundTag = ListTag()

            it.spawners.forEach { data ->
                val tag = CompoundTag()
                tag.putBoolean("activated", data.activated)
                tag.putLong("pos", data.blockPos.asLong())
                tag.put("spawners", data.spawners.toTagList())
                tag.put("drops", data.drops.toTagList())
                tag.putDouble("exp", data.exp)
                dimensionCompoundTag.add(tag)
            }

            compoundTag.put(it.dimension.toString(), dimensionCompoundTag)
        }

        return compoundTag
    }

    private fun load(tag: CompoundTag): SpawnerSavedData {
        tag.allKeys.forEach { dimension ->
            val list = tag.getList(dimension, 9).map { it as CompoundTag }.map {
                CompactSpawnerData(
                    it.getBoolean("activated"),
                    BlockPos.of(it.getLong("pos")),
                    it.getList("spawners", 9).toItemList().toMutableList(),
                    it.getList("drops", 9).toItemList().toMutableList(),
                    it.getDouble("exp")
                )
            }
            dimensionStorages.add(DimensionStorage(ResourceLocation(dimension), list.toMutableList()))

        }
        return this
    }

    fun createSpawner(dimension: ResourceLocation, pos: BlockPos) {
        val spawnerData = CompactSpawnerData(
            false,
            pos,
            mutableListOf(),
            mutableListOf(),
            0.0
        )

        dimensionStorages.find { it.dimension == dimension }?.spawners?.add(spawnerData)
            ?: dimensionStorages.add(DimensionStorage(dimension, mutableListOf(spawnerData)))
    }

    fun deleteSpawner(dimension: ResourceLocation, pos: BlockPos) {
        dimensionStorages
            .find { it.dimension == dimension }
            ?.spawners?.removeIf { it.blockPos == pos }
    }

    fun retrieveSpawner(dimension: ResourceLocation, pos: BlockPos): CompactSpawnerData? {
        return dimensionStorages.find { it.dimension == dimension }?.spawners?.find { it.blockPos == pos }
    }

    inline fun updateSpawner(
        dimension: ResourceLocation,
        pos: BlockPos,
        crossinline block: CompactSpawnerData.() -> Unit
    ) {
        val spawnerData = dimensionStorages
            .find { it.dimension == dimension }
            ?.spawners?.find { it.blockPos == pos }
        spawnerData?.block()
    }
}