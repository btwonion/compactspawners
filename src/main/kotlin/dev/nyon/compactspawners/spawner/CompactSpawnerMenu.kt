package dev.nyon.compactspawners.spawner

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.silkmc.silk.core.entity.blockPos
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setCustomName
import net.silkmc.silk.core.item.setLore
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import net.silkmc.silk.igui.*
import net.silkmc.silk.igui.observable.GuiMutableList
import net.silkmc.silk.igui.observable.asGuiList

fun openCompactSpawnerGUI(player: Player, block: CompactSpawnerTickInterface) {
    igui(GuiType.NINE_BY_SIX, "CompactSpawner".literal, "main") {
        page("main") {
            placeholder(Slots.All, Items.CYAN_STAINED_GLASS_PANE.guiIcon)

            placeholder(Slots.RowOne, Items.ORANGE_STAINED_GLASS_PANE.guiIcon)
            placeholder(Slots.RowSix, Items.ORANGE_STAINED_GLASS_PANE.guiIcon)
            placeholder(Slots.Corners, Items.ORANGE_STAINED_GLASS_PANE.guiIcon)

            button(2 sl 2, itemStack(Items.EXPERIENCE_BOTTLE) {
                setCustomName("Stored exp") { color = ItemColors.NameColor }
                setLore(
                    listOf(
                        literalText("Exp: ${block.exp}") { color = ItemColors.InfoLoreColor },
                        Component.empty(),
                        literalText("Click to collect the stored exp!") { color = ItemColors.ActionLoreColor }
                    )
                )
            }.guiIcon) {
                it.player.giveExperiencePoints(block.exp)
                block.exp = 0
            }

            changePageByKey(5 sl 2, itemStack(Items.SPAWNER) {
                setCustomName("Spawners") { color = ItemColors.NameColor }
                setLore(listOf(
                    literalText("Spawners: ${block.spawners.sumOf { it.count }}") {
                        color = ItemColors.InfoLoreColor
                    },
                    Component.empty(),
                    literalText("Click to open spawner inventory!") { color = ItemColors.ActionLoreColor })
                )
            }.guiIcon, "spawners")

            changePageByKey(8 sl 2, itemStack(Items.IRON_SWORD) {
                setCustomName("Drops") { color = ItemColors.NameColor }
                setLore(listOf(
                    literalText("Items: ${block.mobDrops.sumOf { it.count }}") {
                        color = ItemColors.InfoLoreColor
                    },
                    Component.empty(),
                    literalText("Click to open drops inventory!") { color = ItemColors.ActionLoreColor })
                )
            }.guiIcon, "drops")
        }

        page("spawners") {
            createCompoundTheme()

            val compound = compound((2 sl 2) rectTo (8 sl 5), block.spawners.asGuiList(), { it }) { event, element ->
                if (!event.player.addItem(element)) Block.popResource(
                    event.player.level,
                    event.player.blockPos,
                    element
                )
            }
            createCompoundScroller(compound)
        }

        page("drops") {
            createCompoundTheme()

            val list = GuiMutableList<ItemStack>(block.mobDrops)

            val compound = compound((2 sl 2) rectTo (8 sl 5), list, { it }) { event, element ->
                //TODO allow mutations!!!!
                if (!event.player.addItem(element)) Block.popResource(
                    event.player.level,
                    event.player.blockPos,
                    element
                )
            }
            createCompoundScroller(compound)
        }
    }.startOpen(player)
}

private fun GuiBuilder.PageBuilder.createCompoundTheme() {
    placeholder(Slots.Border, Items.CYAN_STAINED_GLASS_PANE.guiIcon)

    changePageByKey(6 sl 1, itemStack(Items.ARROW) {
        setCustomName("Back") { color = ItemColors.NameColor }
        setLore(listOf(literalText("Click to go back to the main page!") { color = ItemColors.ActionLoreColor }))
    }.guiIcon, "main")
}

private fun GuiBuilder.PageBuilder.createCompoundScroller(compound: GuiCompound<*>) {
    compoundScroll(9 sl 6, itemStack(Items.LIGHTNING_ROD) {
        setCustomName("Scroll") { color = ItemColors.NameColor }
        setLore(
            listOf(
                literalText("Left Click - Up") { color = ItemColors.InfoLoreColor },
                literalText("Right Click - Down") { color = ItemColors.InfoLoreColor }
            )
        )
    }.guiIcon, compound, false)
}

object ItemColors {
    const val NameColor = 0xF0B933
    const val InfoLoreColor = 0x80BA58
    const val ActionLoreColor = 0xBA3F90
}