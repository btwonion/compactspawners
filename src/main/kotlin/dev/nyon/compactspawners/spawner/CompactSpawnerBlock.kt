package dev.nyon.compactspawners.spawner

import dev.nyon.compactspawners.config.config
import dev.nyon.compactspawners.utils.dropExperience
import dev.nyon.compactspawners.utils.random
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import java.util.ArrayList

object CompactSpawnerBlock {

    fun handleNewDrop(
        itemStack: ItemStack,
        mobDrops: ArrayList<ItemStack>,
        level: ServerLevel,
        pos: BlockPos
    ): ArrayList<ItemStack> {
        if (itemStack.isEmpty) return mobDrops
        println("new drop $itemStack")
        mobDrops.find { ItemStack.isSameItemSameTags(itemStack, it) && it.count + itemStack.count <= it.maxStackSize }?.apply {
            this.grow(itemStack.count)
            println("found fitting stack")
            return mobDrops
        }

        mobDrops.filter { ItemStack.isSameItemSameTags(itemStack, it) && it.count < it.maxStackSize }.forEach { item ->
            if (itemStack.count == 0) return mobDrops
            val freeAmount = item.maxStackSize - item.count
            itemStack.shrink(freeAmount)
            item.grow(freeAmount)

            println("added a part to existing stack")
        }

        if (config.maxStoredDrops == -1) {
            println("create new stack cause of infinite space")
            mobDrops.add(itemStack)
            return mobDrops
        }

        if (config.maxStoredDrops > mobDrops.size) {
            if (mobDrops.size + itemStack.count <= config.maxStoredDrops) {
                mobDrops.add(itemStack)
                println("create new stack")
                return mobDrops
            }

            val freeAmount = config.maxStoredDrops - mobDrops.size
            itemStack.shrink(freeAmount)
            mobDrops.add(itemStack.copyWithCount(freeAmount))
            println("create parted stack")
        }

        Block.popResource(level, pos.random(), itemStack)
        println("pop drop")
        return mobDrops
    }

    fun handleNewExp(newExp: Int, exp: Int, pos: BlockPos, level: ServerLevel): Int {
        var updatedExp = exp
        val freeExp = config.maxStoredExp - updatedExp

        if (config.maxStoredExp == -1 || newExp <= freeExp) {
            println("add exp")
            updatedExp += newExp
            return updatedExp
        }

        updatedExp += freeExp
        level.dropExperience(pos.random(), newExp - freeExp)
        println("pop exp")
        return updatedExp
    }
}