package dev.nyon.compactspawners.spawner

import dev.nyon.compactspawners.config.config
import dev.nyon.compactspawners.utils.dropExperience
import dev.nyon.compactspawners.utils.random
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block

object CompactSpawnerBlock {

    fun handleNewDrop(itemStack: ItemStack, mobDrops: NonNullList<ItemStack>, level: ServerLevel, pos: BlockPos) {
        mobDrops.find { it.sameItem(itemStack) && it.count + itemStack.count <= it.maxStackSize }?.also {
            it.count += itemStack.count
            return
        }

        mobDrops.filter { it.sameItem(itemStack) && it.count < it.maxStackSize }.forEach { item ->
            if (itemStack.count == 0) return
            val freeAmount = item.maxStackSize - item.count
            val itemsToAdd = if (itemStack.count >= freeAmount) freeAmount else itemStack.count
            itemStack.count -= itemsToAdd
            item.count += itemsToAdd
        }

        if (config.maxStoredDrops == -1) {
            mobDrops.add(itemStack)
            return
        }

        if (itemStack.count < 0 && config.maxStoredDrops > mobDrops.size) {
            if (mobDrops.size + itemStack.count <= config.maxStoredDrops) {
                mobDrops.add(itemStack)
                return
            }

            itemStack.count -= config.maxStoredDrops - mobDrops.size
            mobDrops.add(itemStack.copyWithCount(config.maxStoredDrops - mobDrops.size))
        }

        Block.popResource(level, pos.random(), itemStack)
    }

    fun handleNewExp(newExp: Int, exp: Int, pos: BlockPos, level: ServerLevel): Int {
        var updatedExp = exp
        if (config.maxStoredExp == -1) {
            updatedExp += newExp
            return updatedExp
        }

        val freeExp = config.maxStoredExp - updatedExp

        if (newExp <= freeExp) {
            updatedExp += freeExp
            return updatedExp
        }

        if (freeExp == 0) {
            level.dropExperience(pos.random(), updatedExp)
            return updatedExp
        }

        updatedExp += freeExp
        level.dropExperience(pos.random(), newExp - freeExp)
        return updatedExp
    }
}