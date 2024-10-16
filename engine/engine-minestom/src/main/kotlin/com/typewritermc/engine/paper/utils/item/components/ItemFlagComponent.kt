package com.typewritermc.engine.paper.utils.item.components

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import org.bukkit.inventory.ItemFlag

@AlgebraicTypeInfo("flag", Colors.BLUE, "material-symbols:flag")
class ItemFlagComponent(
    val flag: ItemFlag,
) : ItemComponent {
    override fun apply(player: Player?, item: ItemStack): ItemStack = item.addItemFlags(flag)
}