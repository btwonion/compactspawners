package dev.nyon.compactspawners.config

import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

fun generateConfigScreen(parent: Screen?): Screen {
    val builder = YetAnotherConfigLib.createBuilder().title(Component.literal("CompactSpawners"))
        .category(generateGeneralCategory())
    builder.save { saveConfig() }
    return builder.build().generateScreen(parent)
}

fun generateGeneralCategory(): ConfigCategory = ConfigCategory.createBuilder().name(Component.literal("General"))
    .option(
        Option.createBuilder<Int>().name(Component.literal("Maximum merged spawners"))
            .description(OptionDescription.of(Component.literal("Decides the amount of spawners, which are allowed in a spawner block")))
            .binding(config.maxMergedSpawners, { config.maxMergedSpawners }, { config.maxMergedSpawners = it })
            .controller { IntegerFieldControllerBuilder.create(it) }.build()
    )
    .option(
        Option.createBuilder<Int>().name(Component.literal("Maximum stored exp"))
            .description(OptionDescription.of(Component.literal("Decides the amount of exp a spawner block should store")))
            .binding(config.maxStoredExp, { config.maxStoredExp }, { config.maxStoredExp = it })
            .controller { IntegerFieldControllerBuilder.create(it) }.build()
    )
    .option(
        Option.createBuilder<Int>().name(Component.literal("Maximum stored drops"))
            .description(OptionDescription.of(Component.literal("Decides the amount of drops a spawner block should store")))
            .binding(config.maxStoredDrops, { config.maxStoredDrops }, { config.maxStoredDrops = it })
            .controller { IntegerFieldControllerBuilder.create(it) }.build()
    )
    .option(
        Option.createBuilder<Boolean>().name(Component.literal("Silk touch spawner break"))
            .description(OptionDescription.of(Component.literal("Decides whether the spawner should be mineable with silk touch enchantment or not")))
            .binding(config.silkBreakSpawners, { config.silkBreakSpawners }, { config.silkBreakSpawners = it })
            .controller { TickBoxControllerBuilder.create(it) }.build()
    )
    .option(
        Option.createBuilder<Int>().name(Component.literal("Required players range"))
            .description(OptionDescription.of(Component.literal("Decides in which range a player should stand nearby a spawner to spawn mobs")))
            .binding(config.requiredPlayerRange, { config.requiredPlayerRange }, { config.requiredPlayerRange = it })
            .controller { IntegerFieldControllerBuilder.create(it) }.build()
    )
    .option(
        Option.createBuilder<Int>().name(Component.literal("Mobs per spawner"))
            .description(OptionDescription.of(Component.literal("Decides how many mobs there should 'spawn' per period")))
            .binding(config.mobsPerSpawner, { config.mobsPerSpawner }, { config.mobsPerSpawner = it })
            .controller { IntegerFieldControllerBuilder.create(it) }.build()
    )
    .build()