package com.typewritermc.basic.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.minestom.adapt.event.EventHandler
import com.typewritermc.engine.minestom.entry.entries.AudienceEntry
import com.typewritermc.engine.minestom.entry.entries.AudienceFilter
import com.typewritermc.engine.minestom.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.minestom.entry.entries.Invertible
import com.typewritermc.engine.minestom.utils.item.Item
import net.minestom.server.entity.Player
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.event.inventory.InventoryItemChangeEvent
import net.minestom.server.event.inventory.InventoryOpenEvent
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.item.PickupItemEvent
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent
import net.minestom.server.event.trait.InventoryEvent

@Entry(
    "holding_item_audience",
    "Filters an audience based on if they are holding a specific item",
    Colors.MEDIUM_SEA_GREEN,
    "mdi:hand"
)
/**
 * The `Holding Item Audience` entry is an audience filter that filters an audience based on if they are holding a specific item.
 * The audience will only contain players that are holding the specified item.
 *
 * ## How could this be used?
 * Could show a path stream to a location when the player is holding a map.
 */
class HoldingItemAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    @Help("The item to check for.")
    val item: Item = Item.Empty,
    override val inverted: Boolean = false,
) : AudienceFilterEntry, Invertible {
    override fun display(): AudienceFilter = HoldingItemAudienceFilter(ref(), item)
}

class HoldingItemAudienceFilter(
    ref: Ref<out AudienceFilterEntry>,
    private val item: Item,
) : AudienceFilter(ref) {
    override fun filter(player: Player): Boolean {
        val holdingItem = player.inventory.itemInMainHand
        return item.isSameAs(player, holdingItem)
    }

    @EventHandler
    fun onPlayerItemHeld(event: PlayerChangeHeldSlotEvent) {
        val player = event.player
        val newHoldingItem = player.inventory.getItemStack(event.slot.toInt())
        player.updateFilter(item.isSameAs(player, newHoldingItem))
    }

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) = onInventoryEvent(event)

    @EventHandler
    fun onInventoryDragEvent(event: InventoryItemChangeEvent) = onInventoryEvent(event)

    @EventHandler
    fun onInventoryOpenEvent(event: InventoryOpenEvent) = onInventoryEvent(event)

    @EventHandler
    fun onInventoryCloseEvent(event: InventoryCloseEvent) = onInventoryEvent(event)

    @EventHandler
    fun onInventoryEvent(event: InventoryEvent) {
        if(event.inventory == null) {
            // player inventory
            return
        }

        for (viewer in event.inventory!!.viewers) {
            viewer.refresh()
        }
    }

    @EventHandler
    fun onPickupItem(event: PickupItemEvent) {
        if(event.livingEntity is Player) {
            (event.livingEntity as Player).refresh()
        }
    }

    @EventHandler
    fun onDropItem(event: ItemDropEvent) = event.player.refresh()
}