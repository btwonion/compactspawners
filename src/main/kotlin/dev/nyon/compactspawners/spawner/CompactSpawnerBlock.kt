package dev.nyon.compactspawners.spawner

import dev.nyon.compactspawners.config.config
import dev.nyon.compactspawners.utils.loadAllItems
import dev.nyon.compactspawners.utils.random
import dev.nyon.compactspawners.utils.saveAllItems
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.SpawnerBlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.phys.BlockHitResult

class CompactSpawnerBlock(properties: BlockBehaviour.Properties) : BaseEntityBlock(properties) {
    val mobDrops: NonNullList<ItemStack> = NonNullList.withSize(config.maxStoredDrops, ItemStack.EMPTY)
    val spawners: NonNullList<ItemStack> = NonNullList.withSize(config.maxStoredDrops, ItemStack.EMPTY)
    var activated: Boolean = false
    var exp: Int = 0
    var entityType: EntityType<*>? = null

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return CompactSpawnerBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(
            blockEntityType,
            BlockEntityType.MOB_SPAWNER
        ) { tickLevel: Level?, pos: BlockPos?, tickState: BlockState?, blockEntity: SpawnerBlockEntity? ->
            if (entityType == null) return@createTickerHelper
            if (tickLevel?.isClientSide == true) SpawnerBlockEntity.clientTick(
                tickLevel,
                pos ?: return@createTickerHelper,
                tickState ?: return@createTickerHelper,
                blockEntity ?: return@createTickerHelper
            )
            else CustomTicker.serverTick(
                tickLevel as ServerLevel,
                pos ?: return@createTickerHelper,
                entityType!!,
                spawners.size,
                {
                    mobDrops.find { it.sameItem(this) && it.count + this.count <= it.maxStackSize }?.also {
                        it.count += this.count
                        return@serverTick
                    }

                    mobDrops.filter { it.sameItem(this) && it.count < it.maxStackSize }.forEach { item ->
                        if (this.count == 0) return@serverTick
                        val freeAmount = item.maxStackSize - item.count
                        val itemsToAdd = if (this.count >= freeAmount) freeAmount else this.count
                        this.count -= itemsToAdd
                        item.count += itemsToAdd
                    }

                    if (config.maxStoredDrops == -1) {
                        mobDrops.add(this)
                        return@serverTick
                    }

                    if (this.count < 0 && config.maxStoredDrops > mobDrops.size) {
                        if (mobDrops.size + this.count <= config.maxStoredDrops) {
                            mobDrops.add(this)
                            return@serverTick
                        }

                        this.count -= config.maxStoredDrops - mobDrops.size
                        mobDrops.add(this.copyWithCount(config.maxStoredDrops - mobDrops.size))
                    }

                    Block.popResource(level, pos.random(), this)
                }
            ) {
                if (config.maxStoredExp == -1) {
                    exp += this
                    return@serverTick
                }

                val freeExp = config.maxStoredExp - exp

                if (this <= freeExp) {
                    exp += freeExp
                    return@serverTick
                }

                if (freeExp == 0) {
                    popExperience(tickLevel, pos.random(), exp)
                    return@serverTick
                }

                exp += freeExp
                popExperience(tickLevel, pos.random(), this - freeExp)
            }
        }
    }

    override fun getDrops(state: BlockState, builder: LootContext.Builder): MutableList<ItemStack> {
        return super.getDrops(state, builder)
            .apply { addAll(listOf(mobDrops.toList(), spawners.toList()).flatMap { it.toList() }) }
    }

    override fun spawnAfterBreak(
        state: BlockState,
        level: ServerLevel,
        pos: BlockPos,
        stack: ItemStack,
        dropExperience: Boolean
    ) {
        popExperience(level, pos, exp)
        listOf(mobDrops, spawners).flatten().forEach {
            Block.popResource(level, pos, it)
        }

        if (dropExperience && !EnchantmentHelper.hasSilkTouch(stack)) {
            val i = 15 + level.random.nextInt(15) + level.random.nextInt(15)
            popExperience(level, pos, i)
        }
    }

    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        activated = true
        return InteractionResult.PASS
    }

    inner class CompactSpawnerBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(BlockEntityType.MOB_SPAWNER, pos, state) {

        override fun load(tag: CompoundTag) {
            activated = tag.getBoolean("activated")
            tag.loadAllItems("drops", mobDrops)
            tag.loadAllItems("spawners", spawners)
            exp = tag.getInt("exp")
            entityType = tag.getString("type")
                .let { if (it == "null") return@let null else BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation(it)) }
        }

        override fun saveAdditional(tag: CompoundTag) {
            tag.putBoolean("activated", activated)
            tag.saveAllItems("drops", mobDrops)
            tag.saveAllItems("spawners", spawners)
            tag.putInt("exp", exp)
            tag.putString(
                "type",
                entityType.let {
                    if (it == null) return@let "null" else BuiltInRegistries.ENTITY_TYPE.getKey(it).toString()
                }
            )

            println(tag)
        }
    }
}
