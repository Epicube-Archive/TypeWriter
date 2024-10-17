package com.typewritermc.basic.entries.fact

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.minestom.entry.entries.GroupEntry
import com.typewritermc.engine.minestom.entry.entries.ReadableFactEntry
import com.typewritermc.engine.minestom.facts.FactData
import com.typewritermc.engine.minestom.utils.item.Item
import net.minestom.server.entity.Player

@Entry(
    "item_in_slot_fact",
    "Check if a specific item is in a specific slot for the player",
    Colors.PURPLE,
    "fa6-solid:hand-holding"
)
/**
 * The `Item In Slot Fact` is a fact that returns the amount of a specific item the player has in a specific slot.
 *
 * <fields.ReadonlyFactInfo />
 *
 * ## How could this be used?
 * Check if the player is wearing a specific armor piece.
 */
class ItemInSlotFact(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
    val item: Item = Item.Empty,
    val slot: Int = 0,
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        val itemInSlot = player.inventory.getItemStack(slot)
        if (!item.isSameAs(player, itemInSlot)) return FactData(0)
        return FactData(itemInSlot.amount())
    }
}