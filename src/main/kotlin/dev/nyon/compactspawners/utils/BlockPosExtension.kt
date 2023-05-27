package dev.nyon.compactspawners.utils

import net.minecraft.core.BlockPos

fun BlockPos.random(offset: Int = 1, noY: Boolean = true, withoutInitialPos: Boolean = true): BlockPos {
    val xRange = (this.x - offset..this.x + offset).toMutableList().also { if (withoutInitialPos) it.remove(this.x) }
    val yRange = (this.y - offset..this.y + offset)
    val zRange = (this.z - offset..this.z + offset).toMutableList().also { if (withoutInitialPos) it.remove(this.z) }

    return BlockPos(xRange.random(), if (noY) this.y else yRange.random(), zRange.random())
}