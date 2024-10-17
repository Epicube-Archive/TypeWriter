package com.typewritermc.basic.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.Modifier
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.engine.minestom.entry.entries.ActionEntry
import com.typewritermc.engine.minestom.utils.ThreadType.SYNC
import com.typewritermc.engine.minestom.utils.item.Item
import com.typewritermc.engine.minestom.utils.toBukkitLocation
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.Player
import net.minestom.server.utils.time.TimeUnit
import java.time.Duration
import java.util.*

@Entry("drop_item", "Drop an item at location, or on player", Colors.RED, "fa-brands:dropbox")
/**
 * The `Drop Item Action` is an action that drops an item in the world.
 * This action provides you with the ability to drop an item with a specified Minecraft material, amount, display name, lore, and location.
 *
 * ## How could this be used?
 *
 * This action can be useful in a variety of situations.
 * You can use it to create treasure chests with randomized items, drop loot from defeated enemies, or spawn custom items in the world.
 * The possibilities are endless!
 */
class DropItemActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val item: Item = Item.Empty,
    @Help("The location to drop the item. (Defaults to the player's location)")
    private val location: Optional<Position> = Optional.empty(),
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)
        // Run on main thread
        SYNC.launch {
            if (location.isPresent) {
                val position = location.get()
                val bukkitLocation = position.toBukkitLocation()

                val droppedItem = item.build(player)

                val itemEntity = ItemEntity(droppedItem)
                itemEntity.setPickupDelay(Duration.of(500, TimeUnit.MILLISECOND))
                itemEntity.setInstance(player.instance, bukkitLocation.position)
                val velocity = player.position.direction().mul(6.0)
                itemEntity.setVelocity(velocity)
            } else {
                player.dropItem(item.build(player))
            }
        }
    }
}