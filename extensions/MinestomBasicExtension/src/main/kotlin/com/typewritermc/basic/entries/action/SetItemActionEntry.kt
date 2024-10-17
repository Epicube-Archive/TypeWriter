package com.typewritermc.basic.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.Modifier
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.engine.minestom.entry.entries.ActionEntry
import com.typewritermc.engine.minestom.utils.item.Item
import com.typewritermc.engine.minestom.utils.ThreadType.SYNC
import net.minestom.server.entity.Player

@Entry("set_item", "Set an item in a specific slot", Colors.RED, "fluent:tray-item-add-24-filled")
/**
 * The `Set Item Action` is an action that sets an item in a specific slot in the player's inventory.
 *
 * ## How could this be used?
 *
 * This can be used to equip a player with an elytra, or give them a weapon.
 */
class SetItemActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val item: Item = Item.Empty,
    val slot: Int = 0,
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        SYNC.launch {
            player.inventory.setItemStack(slot, item.build(player))
        }
    }
}