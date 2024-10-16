package com.typewritermc.engine.paper.utils.item.components

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.*
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

@AlgebraicTypeInfo("amount", Colors.BLUE, "fa6-solid:hashtag")
class ItemAmountComponent(
    @InnerMin(Min(0))
    @Default("1")
    val amount: Int = 1,
): ItemComponent {
    override fun apply(player: Player?, item: ItemStack): ItemStack {
        return item.withAmount(amount)
    }
}