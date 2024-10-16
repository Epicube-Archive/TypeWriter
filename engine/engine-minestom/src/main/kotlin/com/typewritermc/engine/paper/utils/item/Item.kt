package com.typewritermc.engine.paper.utils.item

import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

sealed interface Item {
    fun build(player: Player?): ItemStack
    fun isSameAs(player: Player?, item: ItemStack?): Boolean

    companion object {
        val Empty = CustomItem()
    }
}
