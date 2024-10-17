package com.typewritermc.engine.paper.utils.item.components

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import net.minestom.server.component.DataComponent
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

@AlgebraicTypeInfo("flag", Colors.BLUE, "material-symbols:flag")
class ItemFlagComponent<T : Any>(
    val flag: DataComponent<T>,
    val value: T
) : ItemComponent {
    override fun apply(player: Player?, item: ItemStack): ItemStack = item.with(flag, value)
}