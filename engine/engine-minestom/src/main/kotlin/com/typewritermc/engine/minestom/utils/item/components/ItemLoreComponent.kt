package com.typewritermc.engine.minestom.utils.item.components

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.*
import com.typewritermc.engine.minestom.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.minestom.utils.asMini
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

@AlgebraicTypeInfo("lore", Colors.ORANGE, "flowbite:file-lines-solid")
class ItemLoreComponent(
    @Placeholder
    @Colored
    @MultiLine
    val lore: String,
) : ItemComponent {
    override fun apply(player: Player?, item: ItemStack): ItemStack {
        return item.withLore(lore.parsePlaceholders(player).split("\n").map { it.asMini() })
    }
}