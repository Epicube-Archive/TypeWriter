package com.typewritermc.engine.paper.utils.item.components

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.asMini
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

@AlgebraicTypeInfo("item_name", Colors.ORANGE, "fa6-solid:tag")
class ItemNameComponent(
    @Placeholder
    @Colored
    val name: String,
) : ItemComponent {
    override fun apply(player: Player?, item: ItemStack): ItemStack {
        return item.withCustomName(name.parsePlaceholders(player).asMini())
    }
}