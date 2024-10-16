package com.typewritermc.engine.paper.utils.item.components

import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

sealed interface ItemComponent {
    fun apply(player: Player?, item: ItemStack): ItemStack
}