package dev.nyon.compactspawners.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack

fun List<ItemStack>.toTagList() = this.run {
    ListTag().apply { this@apply.addAll(this@run.map { it.tag }) }
}

fun ListTag.toItemList() = this.map { ItemStack.of(it as CompoundTag) }