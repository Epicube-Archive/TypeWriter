package com.typewritermc.engine.paper.utils.item

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.AlgebraicTypeInfo
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.engine.paper.adapt.deserializeItemFromBytes
import com.typewritermc.engine.paper.adapt.serializeAsBytes
import com.typewritermc.engine.paper.utils.plainText
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
@AlgebraicTypeInfo("serialized_item", Colors.ORANGE, "mingcute:file-code-fill")
class SerializedItem(
    private val material: Material = Material.AIR,
    private val name: String = material.name(),

    @Default("1")
    private val amount: Int = 1,

    private val bytes: String = "", // Base64 encoded bytes
) : Item {
    constructor(itemStack: ItemStack) : this(
        itemStack.material(),
        itemStack.get(ItemComponent.CUSTOM_NAME)?.plainText() ?: itemStack.material().name(),
        itemStack.amount(),
        Base64.encode(itemStack.serializeAsBytes())
    )

    @delegate:Transient
    private val itemStack: ItemStack by lazy(LazyThreadSafetyMode.NONE) {
        val bytes = Base64.decode(bytes)
        deserializeItemFromBytes(bytes).withAmount(this@SerializedItem.amount)
    }

    override fun build(player: Player?): ItemStack = itemStack
    override fun isSameAs(player: Player?, item: ItemStack?): Boolean = item != null && this.itemStack.isSimilar(item)
}

fun ItemStack.toItem(): SerializedItem = SerializedItem(this)