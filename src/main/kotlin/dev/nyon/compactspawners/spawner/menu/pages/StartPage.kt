package dev.nyon.compactspawners.spawner.menu.pages

import dev.nyon.compactspawners.spawner.menu.CSMenuScreen
import dev.nyon.compactspawners.spawner.menu.CompactSpawnerMenu
import dev.nyon.compactspawners.spawner.menu.ItemColors
import dev.nyon.compactspawners.utils.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

fun CompactSpawnerMenu.initStartPage(): Map<Int, ClickEventConsumer> {
    // Create placeholders
    createBorder(ItemStack(Items.BLUE_STAINED_GLASS_PANE).setHoverName(Component.empty()))
    (0..53).toMutableList().also { it.removeAll(borderSlots); it.removeAll(listOf(20, 31, 24)) }.forEach {
        getSlot(it).set(ItemStack(Items.MAGENTA_STAINED_GLASS_PANE).setHoverName(Component.empty()))
    }

    // Create interactable items
    getSlot(20).set(generateExpItem())
    getSlot(31).set(generateSpawnerItem())
    getSlot(24).set(generateDropsItem())

    // Create button listeners
    val expButtonListener: ClickEventConsumer = event@{
        this.player.giveExperiencePoints(spawner.exp)
        spawner.exp = 0
        this.slot.set(generateExpItem())
        return@event true
    }
    val spawnerButtonListener: ClickEventConsumer = event@{
        loadPage(CSMenuScreen.Spawners)
        return@event true
    }
    val dropsButtonListener: ClickEventConsumer = event@{
        loadPage(CSMenuScreen.Drops)
        return@event true
    }

    return mapOf(
        20 to expButtonListener,
        31 to spawnerButtonListener,
        24 to dropsButtonListener
    )
}

private fun CompactSpawnerMenu.generateExpItem(): ItemStack {
    return ItemStack(Items.EXPERIENCE_BOTTLE)
        .setHoverName(Component.literal("Stored exp").withStyle(Style.EMPTY.withColor(ItemColors.NameColor)))
        .setLore(
            listOf(
                Component.literal("Exp: ${spawner.exp}").withStyle(Style.EMPTY.withColor(ItemColors.InfoLoreColor)),
                Component.empty(),
                Component.literal("Click to collect the stored exp!")
                    .withStyle(Style.EMPTY.withColor(ItemColors.ActionLoreColor))
            )
        )
}

private fun CompactSpawnerMenu.generateSpawnerItem(): ItemStack {
    return ItemStack(Items.SPAWNER)
        .setHoverName(Component.literal("Stored spawners").withStyle(Style.EMPTY.withColor(ItemColors.NameColor)))
        .setLore(
            listOf(
                Component.literal("Spawners: ${spawner.spawners.size}")
                    .withStyle(Style.EMPTY.withColor(ItemColors.InfoLoreColor)),
                Component.empty(),
                Component.literal("Click to open the spawner managing gui!")
                    .withStyle(Style.EMPTY.withColor(ItemColors.ActionLoreColor))
            )
        )
}

private fun CompactSpawnerMenu.generateDropsItem(): ItemStack {
    return ItemStack(Items.IRON_SWORD)
        .setHoverName(Component.literal("Stored drops").withStyle(Style.EMPTY.withColor(ItemColors.NameColor)))
        .setLore(
            listOf(
                Component.literal("Drops: ${spawner.mobDrops.size}")
                    .withStyle(Style.EMPTY.withColor(ItemColors.InfoLoreColor)),
                Component.empty(),
                Component.literal("Click to open the drops collector gui!")
                    .withStyle(Style.EMPTY.withColor(ItemColors.ActionLoreColor))
            )
        )
}

fun generateBackItem(): ItemStack {
    return ItemStack(Items.ARROW)
        .setHoverName(Component.literal("Back to overview page").withStyle(Style.EMPTY.withColor(ItemColors.NameColor)))
        .setLore(
            listOf(
                Component.literal("Click to open the overview page!")
                    .withStyle(Style.EMPTY.withColor(ItemColors.ActionLoreColor))
            )
        )
}

fun CompactSpawnerMenu.createBackItemHandler(): ClickEventConsumer = event@{
    loadPage(CSMenuScreen.Start)
    return@event true
}