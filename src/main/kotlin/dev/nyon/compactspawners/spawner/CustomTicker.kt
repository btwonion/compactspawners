package dev.nyon.compactspawners.spawner

import dev.nyon.compactspawners.config.config
import dev.nyon.compactspawners.mixins.MobAccessor
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSources
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BaseSpawner
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets


object CustomTicker {
    private var spawnDelay = 20
    fun serverTick(
        serverLevel: ServerLevel,
        pos: BlockPos,
        entityType: EntityType<*>,
        spawnerCount: Int,
        newDrop: ItemStack.() -> Unit,
        newExp: Int.() -> Unit
    ) {
        if (config.requiredPlayerRange != -1 && !this.isNearPlayer(serverLevel, pos)) return
        if (this.spawnDelay == -1) spawnDelay = 200

        if (this.spawnDelay > 0) {
            --this.spawnDelay
            return
        }

        val mobsToSpawn = config.mobsPerSpawner * (spawnerCount + 1)
        (1.. mobsToSpawn).forEach { _ ->
            val entity = entityType.create(serverLevel) as? Mob ?: return@forEach

            val resourceLocation: ResourceLocation = entity.lootTable
            val lootTable: LootTable = serverLevel.server.lootTables.get(resourceLocation)
            val builder: LootContext.Builder = (entity as MobAccessor).createLootContext(
                true,
                DamageSources(serverLevel.server.registryAccess()).generic()
            )
            lootTable.getRandomItems(
                builder.create(LootContextParamSets.ENTITY)
            ) { stack: ItemStack? -> stack?.newDrop() }

            newExp(entity.experienceReward)
        }
        spawnDelay = 200
    }

    private fun isNearPlayer(level: ServerLevel, pos: BlockPos): Boolean {
        return level.hasNearbyAlivePlayer(
            pos.x.toDouble() + 0.5,
            pos.y.toDouble() + 0.5,
            pos.z.toDouble() + 0.5,
            config.requiredPlayerRange.toDouble()
        )
    }
}

interface CompactSpawnerTickInterface {
    fun serverTick(instance: BaseSpawner, serverLevel: ServerLevel, pos: BlockPos)

    var activated: Boolean
    var mobDrops: NonNullList<ItemStack>
    var spawners: NonNullList<ItemStack>
    var exp: Int
}