package dev.nyon.compactspawners.storage

import net.minecraft.resources.ResourceLocation

data class DimensionStorage(val dimension: ResourceLocation, val spawners: MutableList<CompactSpawnerData>)