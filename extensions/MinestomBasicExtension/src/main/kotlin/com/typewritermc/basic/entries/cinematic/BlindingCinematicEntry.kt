package com.typewritermc.basic.entries.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.cinematic.SimpleCinematicAction
import com.typewritermc.engine.minestom.entry.entries.CinematicAction
import com.typewritermc.engine.minestom.entry.entries.EmptyCinematicAction
import com.typewritermc.engine.minestom.entry.entries.PrimaryCinematicEntry
import com.typewritermc.engine.minestom.entry.entries.Segment
import com.typewritermc.engine.minestom.utils.isFloodgate
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket
import net.minestom.server.network.packet.server.play.CloseWindowPacket

@Entry("blinding_cinematic", "Blind the player so the screen looks black", Colors.CYAN, "heroicons-solid:eye-off")
/**
 * The `Blinding Cinematic` entry is used to blind the player so the screen looks black.
 *
 * ## How could this be used?
 * Make the screen look black for the player during a cinematic.
 */
class BlindingCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(icon = "heroicons-solid:eye-off")
    val segments: List<BlindingSegment> = emptyList(),
) : PrimaryCinematicEntry {
    override fun createSimulating(player: Player): CinematicAction? = null
    override fun create(player: Player): CinematicAction {
        // Disable for bedrock players as it doesn't give the desired effect
        if (player.isFloodgate) return EmptyCinematicAction
        return BlindingCinematicAction(
            player,
            this,
        )
    }
}

data class BlindingSegment(
    override val startFrame: Int,
    override val endFrame: Int,
) : Segment

class BlindingCinematicAction(
    private val player: Player,
    entry: BlindingCinematicEntry,
) : SimpleCinematicAction<BlindingSegment>() {
    override val segments: List<BlindingSegment> = entry.segments

    override suspend fun tickSegment(segment: BlindingSegment, frame: Int) {
        super.tickSegment(segment, frame)

        player.sendPacket(ChangeGameStatePacket(ChangeGameStatePacket.Reason.WIN_GAME, 1f))
    }

    override suspend fun stopSegment(segment: BlindingSegment) {
        super.stopSegment(segment)
        player.sendPacket(CloseWindowPacket(0))
    }


    override suspend fun teardown() {
        super.teardown()
        player.sendPacket(CloseWindowPacket(0))
    }
}
