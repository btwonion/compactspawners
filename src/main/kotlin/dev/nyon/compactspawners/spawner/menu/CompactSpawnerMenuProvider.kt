package dev.nyon.compactspawners.spawner.menu

import dev.nyon.compactspawners.spawner.CompactSpawnerTickInterface
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player

class CompactSpawnerMenuProvider(
    val spawner: CompactSpawnerTickInterface,
    private val initialScreen: CSMenuScreen = CSMenuScreen.Start
) : SimpleContainer(6 * 9), MenuProvider {

    override fun createMenu(i: Int, inventory: Inventory, player: Player): CompactSpawnerMenu? {
        if (player !is ServerPlayer) return null
        return CompactSpawnerMenu(player, spawner).also { it.loadPage(initialScreen) }
    }

    override fun getDisplayName(): Component {
        return Component.literal("CompactSpawner")
            .setStyle(Style.EMPTY.withColor(ItemColors.NameColor))
    }
}