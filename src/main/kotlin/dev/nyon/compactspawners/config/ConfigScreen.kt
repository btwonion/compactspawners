package dev.nyon.compactspawners.config

import dev.isxander.yacl.api.ConfigCategory
import dev.isxander.yacl.api.Option
import dev.isxander.yacl.api.YetAnotherConfigLib
import dev.isxander.yacl.gui.controllers.TickBoxController
import dev.isxander.yacl.gui.controllers.string.number.IntegerFieldController
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
        Option.createBuilder(Int::class.java).name(Component.literal("Maximum merged spawners"))
            .tooltip(Component.literal("Decides the amount of spawners, which are allowed in a spawner block"))
            .binding(config.maxMergedSpawners, { config.maxMergedSpawners }, { config.maxMergedSpawners = it })
            .controller(::IntegerFieldController).build()
    )
    .option(
        Option.createBuilder(Int::class.java).name(Component.literal("Maximum stored exp"))
            .tooltip(Component.literal("Decides the amount of exp a spawner block should store"))
            .binding(config.maxStoredExp, { config.maxStoredExp }, { config.maxStoredExp = it })
            .controller(::IntegerFieldController).build()
    )
    .option(
        Option.createBuilder(Int::class.java).name(Component.literal("Maximum stored drops"))
            .tooltip(Component.literal("Decides the amount of drops a spawner block should store"))
            .binding(config.maxStoredDrops, { config.maxStoredDrops }, { config.maxStoredDrops = it })
            .controller(::IntegerFieldController).build()
    )
    .option(
        Option.createBuilder(Boolean::class.java).name(Component.literal("Silk touch spawner break"))
            .tooltip(Component.literal("Decides whether the spawner should be mineable with silk touch enchantment or not"))
            .binding(config.silkBreakSpawners, { config.silkBreakSpawners }, { config.silkBreakSpawners = it })
            .controller(::TickBoxController).build()
    )
    .build()