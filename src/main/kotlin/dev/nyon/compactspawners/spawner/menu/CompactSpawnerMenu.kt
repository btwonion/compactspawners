package dev.nyon.compactspawners.spawner.menu

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

class CompactSpawnerMenu(
    val player: ServerPlayer,
    var screen: CSMenuScreen = CSMenuScreen.Start
) : AbstractContainerMenu(MenuType.GENERIC_9x6, 9 * 6) {

    fun loadPage() {

    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }
}