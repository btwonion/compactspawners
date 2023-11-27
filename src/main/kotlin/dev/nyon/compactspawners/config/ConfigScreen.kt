@file:Suppress("SpellCheckingInspection")

package dev.nyon.compactspawners.config

import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.nyon.konfig.config.saveConfig
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

fun generateConfigScreen(parent: Screen?): Screen {
    val builder = YetAnotherConfigLib.createBuilder().title(Component.translatable("menu.compactspawners.name"))
        .category(generateGeneralCategory())
    builder.save { saveConfig(config) }
    return builder.build().generateScreen(parent)
}

fun generateGeneralCategory(): ConfigCategory =
    ConfigCategory.createBuilder().name(Component.translatable("menu.compactspawners.general.name"))
        .option(
            Option.createBuilder<Int>().name(Component.translatable("menu.compactspawners.general.maxmergedspawners"))
                .description(OptionDescription.of(Component.translatable("menu.compactspawners.general.maxmergedspawners.description")))
                .binding(config.maxMergedSpawners, { config.maxMergedSpawners }, { config.maxMergedSpawners = it })
                .controller { IntegerFieldControllerBuilder.create(it) }.build()
        )
        .option(
            Option.createBuilder<Int>().name(Component.translatable("menu.compactspawners.general.maxstoredexp"))
                .description(OptionDescription.of(Component.translatable("menu.compactspawners.general.maxstoredexp.description")))
                .binding(config.maxStoredExp, { config.maxStoredExp }, { config.maxStoredExp = it })
                .controller { IntegerFieldControllerBuilder.create(it) }.build()
        )
        .option(
            Option.createBuilder<Boolean>()
                .name(Component.translatable("menu.compactspawners.general.silkspawnerbreak"))
                .description(OptionDescription.of(Component.translatable("menu.compactspawners.general.silkspawnerbreak.description")))
                .binding(config.silkBreakSpawners, { config.silkBreakSpawners }, { config.silkBreakSpawners = it })
                .controller { TickBoxControllerBuilder.create(it) }.build()
        )
        .option(
            Option.createBuilder<Int>().name(Component.translatable("menu.compactspawners.general.requiredplayerrange"))
                .description(OptionDescription.of(Component.translatable("menu.compactspawners.general.requiredplayerrange.description")))
                .binding(
                    config.requiredPlayerDistance,
                    { config.requiredPlayerDistance },
                    { config.requiredPlayerDistance = it })
                .controller { IntegerFieldControllerBuilder.create(it) }.build()
        )
        .option(
            Option.createBuilder<Int>().name(Component.translatable("menu.compactspawners.general.mobsperspawner"))
                .description(OptionDescription.of(Component.translatable("menu.compactspawners.general.mobsperspawner.description")))
                .binding(config.mobsPerSpawner, { config.mobsPerSpawner }, { config.mobsPerSpawner = it })
                .controller { IntegerFieldControllerBuilder.create(it) }.build()
        )
        .option(
            Option.createBuilder<Float>().name(Component.translatable("menu.compactspawners.general.luck"))
                .description(OptionDescription.of(Component.translatable("menu.compactspawners.general.luck.description")))
                .binding(config.luck, { config.luck }, { config.luck = it })
                .controller { FloatFieldControllerBuilder.create(it) }.build()
        )
        .build()