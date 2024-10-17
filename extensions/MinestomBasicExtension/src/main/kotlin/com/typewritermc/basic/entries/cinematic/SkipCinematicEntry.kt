package com.typewritermc.basic.entries.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.engine.minestom.adapt.event.Listener
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.cinematic.SimpleCinematicAction
import com.typewritermc.engine.minestom.entry.cinematic.setCinematicFrame
import com.typewritermc.engine.minestom.entry.entries.CinematicAction
import com.typewritermc.engine.minestom.entry.entries.CinematicEntry
import com.typewritermc.engine.minestom.entry.entries.Segment
import com.typewritermc.engine.minestom.interaction.InterceptionBundle
import com.typewritermc.engine.minestom.interaction.interceptPackets
import com.typewritermc.engine.minestom.plugin
import com.typewritermc.engine.minestom.utils.callEvent
import com.typewritermc.engine.minestom.utils.uniqueId
import lirand.api.extensions.events.SimpleListener
import lirand.api.extensions.events.listen
import lirand.api.extensions.events.unregister
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent
import net.minestom.server.event.trait.PlayerEvent

@Entry("skip_cinematic", "Allows players to manually skip the cinematic", Colors.RED, "mdi:skip-next")
/**
 * The `Skip Cinematic` entry is used to allow players to manually skip the cinematic.
 *
 * While a segment is active, if the player dismounts,
 * or presses the confirmation key, the cinematic will be skipped to the end of the segment.
 * This allows you to skip only parts of the cinematic.
 * While other parts are not possible to skip.
 *
 * ## How could this be used?
 * On long cinematics, players may want to skip (parts of) the cinematic.
 */
class SkipCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    val confirmationKey: SkipConfirmationKey = SkipConfirmationKey.SNEAK,
    @Segments(icon = "mdi:skip-next", color = Colors.RED)
    val segments: List<SkipSegment> = emptyList(),
) : CinematicEntry {
    override fun create(player: Player): CinematicAction {
        return SkipCinematicAction(player, this)
    }
}

data class SkipSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
) : Segment

enum class SkipConfirmationKey(val keybind: String) {
    SNEAK("<key:key.sneak>"),
    SWAP_HANDS("<key:key.swapOffhand>"),
    ;
}

class SkipCinematicAction(
    private val player: Player,
    val entry: SkipCinematicEntry,
) : SimpleCinematicAction<SkipSegment>() {
    override val segments: List<SkipSegment> = entry.segments
    private var bundle: InterceptionBundle? = null
    private var listener: Listener? = null

    override suspend fun startSegment(segment: SkipSegment) {
        super.startSegment(segment)

        when (entry.confirmationKey) {
            SkipConfirmationKey.SNEAK -> {
                bundle = player.interceptPackets {
                    Play.Client.ENTITY_ACTION { event ->
                        val packet = WrapperPlayClientEntityAction(event)
                        if (packet.entityId != player.entityId) return@ENTITY_ACTION
                        if (packet.action != WrapperPlayClientEntityAction.Action.START_SNEAKING) return@ENTITY_ACTION
                        player.setCinematicFrame(segment.endFrame)
                    }
                }
            }

            SkipConfirmationKey.SWAP_HANDS -> {
                val listener = SimpleListener()
                this.listener = listener
                plugin.listen<PlayerChangeHeldSlotEvent>(listener) {
                    if (it.player.uniqueId != player.uniqueId) return@listen
                    player.setCinematicFrame(segment.endFrame)
                }
            }
        }

        CinematicSkippableEvent(player, true, entry.confirmationKey).callEvent()
    }

    override suspend fun stopSegment(segment: SkipSegment) {
        super.stopSegment(segment)
        CinematicSkippableEvent(player, false, entry.confirmationKey).callEvent()
        bundle?.cancel()
        bundle = null
        listener?.unregister()
        listener = null
    }
}

class CinematicSkippableEvent(val player: Player, val canSkip: Boolean, val confirmationKey: SkipConfirmationKey) : PlayerEvent {

    override fun getPlayer(): Player {
        return player
    }
}
