package dev.nyon.compactspawners.spawner.menu

import dev.nyon.compactspawners.spawner.CompactSpawnerTickInterface
import dev.nyon.compactspawners.spawner.menu.pages.initDropsPage
import dev.nyon.compactspawners.spawner.menu.pages.initSpawnersPage
import dev.nyon.compactspawners.spawner.menu.pages.initStartPage
import dev.nyon.compactspawners.utils.ClickEvent
import dev.nyon.compactspawners.utils.ClickEventConsumer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

class CompactSpawnerMenu(
    val player: ServerPlayer,
    val spawner: CompactSpawnerTickInterface
) : AbstractContainerMenu(MenuType.GENERIC_9x6, 9 * 6) {

    internal var dropsOffset = 0
    internal var spawnersOffset = 0
    private val listeners: MutableMap<Int, ClickEventConsumer> = mutableMapOf()

    fun loadPage(screen: CSMenuScreen) {
        listeners.clear()
        slots.forEach { it.set(ItemStack.EMPTY) }

        when (screen) {
            CSMenuScreen.Start -> listeners.putAll(initStartPage())
            CSMenuScreen.Spawners -> listeners.putAll(initSpawnersPage())
            CSMenuScreen.Drops -> listeners.putAll(initDropsPage())
        }
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        val listener = listeners[slotId]
        if (listener == null || player !is ServerPlayer) {
            super.clicked(slotId, button, clickType, player)
            return
        }

        val callback = listener.invoke(ClickEvent(getSlot(slotId), button, clickType, player))
        if (!callback) super.clicked(slotId, button, clickType, player)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasItem()) {
            val itemStack2 = slot.item
            itemStack = itemStack2.copy()
            if (index < 6 * 9)
                if (!moveItemStackTo(itemStack2, 6 * 9, slots.size, true))
                    return ItemStack.EMPTY
                else if (!moveItemStackTo(itemStack2, 0, 6 * 9, false))
                    return ItemStack.EMPTY
            if (itemStack2.isEmpty)
                slot.setByPlayer(ItemStack.EMPTY)
            else
                slot.setChanged()
        }

        return itemStack!!
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }
}


enum class CSMenuScreen {
    Start,
    Drops,
    Spawners
}

object ItemColors {
    const val NameColor = 0xF0B933
    const val InfoLoreColor = 0x80BA58
    const val ActionLoreColor = 0xBA3F90
}