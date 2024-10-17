package com.typewritermc.basic.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MaterialProperties
import com.typewritermc.core.extension.annotations.MaterialProperty.BLOCK
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.minestom.adapt.Location
import com.typewritermc.engine.minestom.adapt.event.bukkit.PlayerInteractEvent
import com.typewritermc.engine.minestom.entry.*
import com.typewritermc.engine.minestom.entry.entries.EventEntry
import com.typewritermc.engine.minestom.utils.item.Item
import com.typewritermc.engine.minestom.utils.toVector
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import java.util.*

@Entry("on_interact_with_block", "When the player interacts with a block", Colors.YELLOW, "mingcute:finger-tap-fill")
/**
 * The `Interact Block Event` is triggered when a player interacts with a block by right-clicking it.
 *
 * ## How could this be used?
 *
 * This could be used to create special interactions with blocks, such as opening a secret door when you right-click a certain block, or a block that requires a key to open.
 */
class InteractBlockEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @MaterialProperties(BLOCK)
    val block: Block = Block.AIR,
    val location: Optional<Position> = Optional.empty(),
    @Help("The item the player must be holding when the block is interacted with.")
    val itemInHand: Item = Item.Empty,
    @Help("""
        Cancel the event when triggered.
        It will only cancel the event if all the criteria are met.
        If set to false, it will not modify the event.
    """)
    val cancel: Boolean = false,
    val interactionType: InteractionType = InteractionType.ALL,
    val shiftType: ShiftType = ShiftType.ANY,
) : EventEntry

enum class ShiftType {
    ANY,
    SHIFT,
    NO_SHIFT;

    fun isApplicable(player: Player): Boolean {
        return when (this) {
            ANY -> true
            SHIFT -> player.isSneaking
            NO_SHIFT -> !player.isSneaking
        }
    }
}

enum class InteractionType(vararg val actions: PlayerInteractEvent.Action) {
    ALL(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, PlayerInteractEvent.Action.LEFT_CLICK_BLOCK/*, PlayerInteractEvent.Action.PHYSICAL*/),
    CLICK(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, PlayerInteractEvent.Action.LEFT_CLICK_BLOCK),
    RIGHT_CLICK(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK),
    LEFT_CLICK(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK),
    PHYSICAL(/*PlayerInteractEvent.Action.PHYSICAL*/),
}

private fun hasItemInHand(player: Player, item: Item): Boolean {
    return item.isSameAs(player, player.inventory.itemInMainHand) || item.isSameAs(
        player,
        player.inventory.itemInOffHand
    )
}

fun Location.isSameBlock(location: Location): Boolean {
    return this.world == location.world && this.blockX == location.blockX && this.blockY == location.blockY && this.blockZ == location.blockZ
}

@EntryListener(InteractBlockEventEntry::class)
fun onInteractBlock(event: PlayerInteractEvent, query: Query<InteractBlockEventEntry>) {
    if (event.clickedBlock == null) return
    // The even triggers twice. Both for the main hand and offhand.
    // We only want to trigger once.
    if (event.hand != Player.Hand.MAIN) return // Disable off-hand interactions
    val entries = query.findWhere { entry ->
        // Check if the player is sneaking
        if (!entry.shiftType.isApplicable(event.player)) return@findWhere false

        // Check if the player is interacting with the block in the correct way
        if (!entry.interactionType.actions.contains(event.action)) return@findWhere false

        // Check if the player clicked on the correct location
        if (!entry.location.map { it.sameBlock(event.clickedBlockPosition!!.asVec().toVector()) }
                .orElse(true)) return@findWhere false

        // Check if the player is holding the correct item
        if (!hasItemInHand(event.player, entry.itemInHand)) return@findWhere false

        entry.block == event.clickedBlock
    }.toList()
    if (entries.isEmpty()) return

    entries startDialogueWithOrNextDialogue event.player
    if (entries.any { it.cancel }) event.isCancelled = true
}