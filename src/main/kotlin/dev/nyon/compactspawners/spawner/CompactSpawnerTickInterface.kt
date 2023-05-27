package dev.nyon.compactspawners.spawner

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.ArrayList

interface CompactSpawnerTickInterface {
    fun use(player: Player)

    var activated: Boolean
    var mobDrops: ArrayList<ItemStack>
    var spawners: ArrayList<ItemStack>
    var exp: Int
}