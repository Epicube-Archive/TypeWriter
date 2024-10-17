package com.typewritermc.basic.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.minestom.entry.entries.*
import com.typewritermc.core.entries.ref
import com.typewritermc.engine.minestom.utils.item.Item
import net.minestom.server.entity.Player

@Entry(
    "item_in_inventory_audience",
    "Filters an audience based on if they have a specific item in their inventory",
    Colors.MEDIUM_SEA_GREEN,
    "mdi:bag-personal"
)
/**
 * The `Item In Inventory Audience` entry filters an audience based on if they have a specific item in their inventory.
 *
 * ## How could this be used?
 * This could show a boss bar or sidebar based on if a player has a specific item in their inventory.
 */
class ItemInInventoryAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    val item: Item = Item.Empty,
    override val inverted: Boolean = false,
) : AudienceFilterEntry, Invertible {
    override fun display(): AudienceFilter = ItemInInventoryAudienceFilter(ref(), item)
}

class ItemInInventoryAudienceFilter(
    ref: Ref<out AudienceFilterEntry>,
    private val item: Item,
) : AudienceFilter(ref), TickableDisplay {
    override fun filter(player: Player): Boolean {
        return player.inventory.itemStacks.any { item.isSameAs(player, it) } || item.isSameAs(player, player.inventory.cursorItem)
    }

    override fun tick() {
        consideredPlayers.forEach { it.refresh() }
    }
}
