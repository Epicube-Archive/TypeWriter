package com.typewritermc.engine.minestom.content.modes.custom

import com.typewritermc.engine.minestom.content.ContentContext
import com.typewritermc.engine.minestom.content.modes.ImmediateFieldValueContentMode
import com.typewritermc.engine.minestom.utils.item.Item
import com.typewritermc.engine.minestom.utils.item.toItem
import net.minestom.server.entity.Player
import java.lang.reflect.Type

class HoldingItemContentMode(context: ContentContext, player: Player) :
    ImmediateFieldValueContentMode<Item>(context, player) {
    override val type: Type = Item::class.java

    override fun value(): Item {
        val item = player.inventory.itemInMainHand
        return item.toItem()
    }
}