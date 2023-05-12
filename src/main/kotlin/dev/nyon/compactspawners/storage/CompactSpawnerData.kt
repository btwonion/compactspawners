package dev.nyon.compactspawners.storage

import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack

data class CompactSpawnerData(
    var activated: Boolean,
    var blockPos: BlockPos,
    var spawners: MutableList<ItemStack>,
    var drops: MutableList<ItemStack>,
    var exp: Double
)
