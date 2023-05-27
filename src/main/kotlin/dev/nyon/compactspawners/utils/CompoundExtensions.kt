package dev.nyon.compactspawners.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import java.util.ArrayList

fun CompoundTag.loadAllItems(key: String, list: ArrayList<ItemStack>) {
    val listTag: ListTag = this.getList(key, 10)

    listTag.indices.forEach {
        val compoundTag = listTag.getCompound(it)
        list.add(ItemStack.of(compoundTag))
    }
}

fun CompoundTag.saveAllItems(key: String, list: ArrayList<ItemStack>) {
    val listTag = ListTag()

    list.forEach {
        val compoundTag = CompoundTag()
        it.save(compoundTag)
        listTag.add(compoundTag)
    }

    this.put(key, listTag)
}