package com.typewritermc.basic.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.MaterialProperties
import com.typewritermc.core.extension.annotations.MaterialProperty.BLOCK
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.minestom.entry.*
import com.typewritermc.engine.minestom.entry.entries.EventEntry
import com.typewritermc.engine.minestom.utils.toMinestomPos
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import java.util.*

@Entry("on_place_block", "When the player places a block", Colors.YELLOW, "fluent:cube-add-20-filled")
/**
 * The `Block Place Event` is called when a block is placed in the world.
 *
 * ## How could this be used?
 *
 * This event could be used to create a custom block that has special properties when placed in the world, like particles or sounds that play. It could also be used to create a block that when placed, can turn itself into a cool structure.
 */
class BlockPlaceEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val location: Optional<Position> = Optional.empty(),
    @MaterialProperties(BLOCK)
    val block: Block = Block.STONE,
) : EventEntry

@EntryListener(BlockPlaceEventEntry::class)
fun onPlaceBlock(event: PlayerBlockPlaceEvent, query: Query<BlockPlaceEventEntry>) {
    val position = event.blockPosition.asVec().asPosition()
    query findWhere { entry ->
        // Check if the player clicked on the correct location
        if (!entry.location.map { it.toMinestomPos() == position }.orElse(true)) return@findWhere false

        entry.block == event.block
    } startDialogueWithOrNextDialogue event.player
}