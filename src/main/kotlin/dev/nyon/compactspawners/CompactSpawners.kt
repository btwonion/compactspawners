package dev.nyon.compactspawners

import dev.nyon.compactspawners.config.config
import dev.nyon.compactspawners.config.loadConfig
import dev.nyon.compactspawners.config.saveConfig
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.minecraft.advancements.critereon.EnchantmentPredicate
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction
import net.minecraft.world.level.storage.loot.predicates.MatchTool
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

val json = Json {
    prettyPrint = true
    encodeDefaults = true
}

@Suppress("unused")
object CompactSpawners : ModInitializer {
    @Suppress("SpellCheckingInspection")
    const val modID = "compactspawners"

    override fun onInitialize() {
        loadConfig()
        handleSpawnerLootTable()
    }

    fun shutdown() {
        saveConfig()
    }

    private fun handleSpawnerLootTable() {
        LootTableEvents.MODIFY.register(LootTableEvents.Modify { _, _, id, tableBuilder, _ ->
            if (!config.silkBreakSpawners) return@Modify
            if (id != ResourceLocation("blocks/spawner")) return@Modify
            val builder: LootPool.Builder = LootPool.Builder()
            builder.setRolls(ConstantValue.exactly(1f))
                .with(LootItem.lootTableItem(Items.SPAWNER).build())
                .apply(
                    CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                        .copy("SpawnData", "BlockEntityTag.SpawnData")
                        .copy("SpawnPotentials", "BlockEntityTag.SpawnPotentials")
                        .build()
                )
                .conditionally(
                    MatchTool.toolMatches(
                        ItemPredicate.Builder.item().hasEnchantment(
                            EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))
                        )
                    ).build()
                )
            tableBuilder.pool(builder.build())
        })
    }
}