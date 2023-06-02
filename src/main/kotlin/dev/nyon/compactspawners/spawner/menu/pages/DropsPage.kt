package dev.nyon.compactspawners.spawner.menu.pages

import dev.nyon.compactspawners.spawner.menu.CompactSpawnerMenu
import dev.nyon.compactspawners.utils.ClickEventConsumer
import dev.nyon.compactspawners.utils.InventoryClickTypes
import dev.nyon.compactspawners.utils.createBorder
import dev.nyon.compactspawners.utils.generateScroller
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

fun CompactSpawnerMenu.initDropsPage(): Map<Int, ClickEventConsumer> {
    var maxOffset = 0

    // create border
    createBorder(ItemStack(Items.BLUE_STAINED_GLASS_PANE).setHoverName(Component.empty()))
    getSlot(45).set(generateBackItem())
    getSlot(8).set(generateScroller("drops"))

    // create scroll handler
    val scrollListener: ClickEventConsumer = event@{
        if (this.button == InventoryClickTypes.LeftClick) if (dropsOffset > 0) dropsOffset -= 1
        if (this.button == InventoryClickTypes.RightClick) if (dropsOffset + 1 <= maxOffset) dropsOffset += 1
        return@event true
    }
    // create scrolling area


    return mapOf(
        8 to scrollListener,
        45 to createBackItemHandler()
    )
}