package dev.nyon.compactspawners.spawner

import dev.nyon.compactspawners.CompactSpawners
import dev.nyon.compactspawners.config.config
import dev.nyon.compactspawners.utils.dropExperience
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BaseSpawner
import net.minecraft.world.level.Level
import net.minecraft.world.level.SpawnData
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState

class CompactSpawnerEntity(pos: BlockPos, blockState: BlockState) :
    BaseContainerBlockEntity(CompactSpawners.blockEntityType, pos, blockState), WorldlyContainer {
    companion object {
        fun clientTick(level: Level, pos: BlockPos, blockEntity: CompactSpawnerEntity) {
            blockEntity.spawner.clientTick(level, pos)
        }

        fun serverTick(level: ServerLevel, pos: BlockPos, blockEntity: CompactSpawnerEntity) {
            blockEntity.spawner.serverTick(level, pos)
        }
    }

    var spawner = object : BaseSpawner() {
        override fun broadcastEvent(level: Level, pos: BlockPos, eventId: Int) {
            level.blockEvent(pos, Blocks.SPAWNER, eventId, 0)
        }

        override fun setNextSpawnData(level: Level?, pos: BlockPos, nextSpawnData: SpawnData) {
            super.setNextSpawnData(level, pos, nextSpawnData)
            level?.sendBlockUpdated(pos, blockState, blockState, 4)
        }
    }

    var activated: Boolean = false
    var storedDrops: NonNullList<ItemStack> = NonNullList.withSize(6 * 9, ItemStack.EMPTY)
    var spawnerCount: Int = 1
    var storedExp: Int = 0

    override fun load(tag: CompoundTag) {
        spawner.load(level, blockPos, tag)
        activated = tag.getBoolean("activated")
        storedDrops = NonNullList.withSize(6 * 9, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(tag, this.storedDrops)
        spawnerCount = tag.getInt("spawnerCount")
        storedExp = tag.getInt("storedExp")
    }

    override fun saveAdditional(tag: CompoundTag) {
        spawner.save(tag)
        tag.putBoolean("activated", activated)
        ContainerHelper.saveAllItems(tag, this.storedDrops)
        tag.putInt("spawnerCount", spawnerCount)
        tag.putInt("storedExp", storedExp)
    }

    override fun getContainerSize(): Int {
        return 6 * 9
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        return (0..<6 * 9).toList().toIntArray()
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean {
        return false
    }

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean {
        return this.storedDrops.isNotEmpty()
    }

    override fun isEmpty(): Boolean {
        return this.storedDrops.all { it.isEmpty }
    }

    override fun getItem(slot: Int): ItemStack {
        return this.storedDrops[slot]
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val itemStack = ContainerHelper.removeItem(this.storedDrops, slot, amount)
        if (!itemStack.isEmpty) this.setChanged()
        return itemStack
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return ContainerHelper.takeItem(this.storedDrops, slot)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        this.storedDrops[slot] = stack
        if (stack.count > this.maxStackSize) stack.count = this.maxStackSize
        this.setChanged()
    }

    override fun stillValid(player: Player): Boolean {
        return Container.stillValidBlockEntity(this, player)
    }

    override fun clearContent() {
        this.storedDrops.clear()
    }

    override fun canOpen(player: Player): Boolean {
        return super.canOpen(player)
    }

    override fun getDisplayName(): Component {
        return Component.translatable("menu.compactspawners.name")
    }

    override fun getDefaultName(): Component {
        return Component.translatable("menu.compactspawners.name")
    }

    override fun createMenu(containerId: Int, inventory: Inventory): AbstractContainerMenu {
        return ChestMenu.sixRows(containerId, inventory, this)
    }

    private fun getFreeSlot(): Int {
        return this.storedDrops.indices.first {
            this.storedDrops.isEmpty()
        }
    }

    override fun getUpdateTag(): CompoundTag {
        val compoundTag = saveWithoutMetadata()
        compoundTag.remove("SpawnPotentials")
        return compoundTag
    }

    override fun triggerEvent(id: Int, type: Int): Boolean {
        return if (spawner.onEventTriggered(level!!, id)) true else super.triggerEvent(id, type)
    }

    override fun onlyOpCanSetNbt(): Boolean {
        return true
    }

    override fun getUpdatePacket(): ClientboundBlockEntityDataPacket {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    fun handleNewDrop(item: ItemStack, level: ServerLevel, pos: BlockPos) {
        if (item.isEmpty) return
        if (item.isDamaged) {
            this.storedDrops[this.getFreeSlot()] = item
            return
        }

        this.storedDrops.filter {
            it.count < it.maxStackSize && !it.isDamaged && ItemStack.isSameItemSameTags(item, it)
        }.forEach {
            val freeAmount = it.maxStackSize - it.count
            if (freeAmount < item.count) {
                it.grow(freeAmount)
                item.shrink(freeAmount)
            } else {
                it.grow(item.count)
                return
            }
        }

        this.storedDrops.indexOfFirst { it.isEmpty }.also {
            if (it == -1) return@also
            this.storedDrops[it] = item
            return
        }

        Block.popResource(level, pos, item)
    }

    fun handleNewExp(amount: Int, level: ServerLevel, pos: BlockPos) {
        val freeExp = config.maxStoredExp - storedExp

        if (config.maxStoredExp == -1 || amount <= freeExp) {
            storedExp += amount
            return
        }

        storedExp += freeExp
        level.dropExperience(pos, amount - freeExp)
    }

    fun setEntityId(type: EntityType<*>, random: RandomSource) {
        spawner.setEntityId(type, level, random, worldPosition)
    }
}