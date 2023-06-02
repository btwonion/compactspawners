package dev.nyon.compactspawners.utils

import dev.nyon.compactspawners.spawner.menu.CompactSpawnerMenu
import dev.nyon.compactspawners.spawner.menu.ItemColors
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

val borderSlots = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 53)

fun CompactSpawnerMenu.createBorder(itemStack: ItemStack) {
    borderSlots.forEach {
        this.getSlot(it).set(itemStack)
    }
}

data class ClickEvent(val slot: Slot, val button: Int, val clickType: ClickType, val player: ServerPlayer)

typealias ClickEventConsumer = ClickEvent.() -> Boolean

object InventoryClickTypes {
    const val LeftClick = 0
    const val RightClick = 1
    const val MiddleClick = 2
}

fun generateScroller(pageContents: String): ItemStack {
    return ItemStack(Items.LIGHTNING_ROD)
        .setHoverName(Component.literal("Item scroller").withStyle(Style.EMPTY.withColor(ItemColors.NameColor)))
        .setLore(
            listOf(
                Component.literal("Use this item to scroll between the $pageContents!")
                    .withStyle(Style.EMPTY.withColor(ItemColors.InfoLoreColor)),
                Component.literal("  Right click - scroll down")
                    .withStyle(Style.EMPTY.withColor(ItemColors.InfoLoreColor)),
                Component.literal("  Left click - scroll up")
                    .withStyle(Style.EMPTY.withColor(ItemColors.InfoLoreColor)),
                Component.empty(),
                Component.literal("Click to scroll!").withStyle(Style.EMPTY.withColor(ItemColors.ActionLoreColor))
            )
        )
}