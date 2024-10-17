package com.typewritermc.engine.minestom.utils.item.components

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import com.typewritermc.core.extension.annotations.MaterialProperties
import com.typewritermc.core.extension.annotations.MaterialProperty
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

@AlgebraicTypeInfo("material", Colors.BLUE, "fa6-solid:cube")
class ItemMaterialComponent(
    @MaterialProperties(MaterialProperty.ITEM)
    val material: Material = Material.STONE,
) : ItemComponent {
    override fun apply(player: Player?, item: ItemStack): ItemStack {
        return item.withMaterial(material)
    }
}