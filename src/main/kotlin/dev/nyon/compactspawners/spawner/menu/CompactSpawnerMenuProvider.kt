package dev.nyon.compactspawners.spawner.menu

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

class CompactSpawnerMenuSession(var screen: CSMenuScreen = CSMenuScreen.Start) : SimpleContainer(6 * 9), MenuProvider {

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu? {
        if (player !is ServerPlayer) return null
        return CompactSpawnerMenu(player, screen)
    }

    override fun getDisplayName(): Component {
        return Component.literal("CompactSpawners - ${screen.title}")
            .setStyle(Style.EMPTY.withColor(ItemColors.NameColor))
    }
}

enum class CSMenuScreen(val title: String) {
    Start("Overview"),
    Drops("Drops"),
    Spawners("Spawners")
}

object ItemColors {
    const val NameColor = 0xF0B933
    const val InfoLoreColor = 0x80BA58
    const val ActionLoreColor = 0xBA3F90
}