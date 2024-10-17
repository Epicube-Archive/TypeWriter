package com.typewritermc.basic.entries.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.cinematic.SimpleCinematicAction
import com.typewritermc.engine.minestom.entry.entries.CinematicAction
import com.typewritermc.engine.minestom.entry.entries.CinematicEntry
import com.typewritermc.engine.minestom.entry.entries.Segment
import com.typewritermc.engine.minestom.utils.toBukkitLocation
import com.typewritermc.engine.minestom.utils.toMinestomPos
import com.typewritermc.engine.minestom.utils.toPacketVector3i
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.BlockChangePacket

@Entry("set_fake_block_cinematic", "Set a fake block", Colors.CYAN, "mingcute:cube-3d-fill")
class SetFakeBlockCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(icon = "mingcute:cube-3d-fill")
    @Help("The segments that will be displayed in the cinematic")
    val segments: List<SetFakeBlockSegment> = emptyList(),
) : CinematicEntry {
    override fun create(player: Player): CinematicAction {
        return SetFakeBlockCinematicAction(player, this)
    }
}

data class SetFakeBlockSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
    val location: Position = Position.ORIGIN,
    val block: Block = Block.AIR,
) : Segment

class SetFakeBlockCinematicAction(
    private val player: Player,
    entry: SetFakeBlockCinematicEntry,
) : SimpleCinematicAction<SetFakeBlockSegment>() {
    override val segments: List<SetFakeBlockSegment> = entry.segments

    override suspend fun startSegment(segment: SetFakeBlockSegment) {
        super.startSegment(segment)

        player.sendPacket(BlockChangePacket(
            segment.location.toMinestomPos(),
            segment.block
        ))
    }

    override suspend fun stopSegment(segment: SetFakeBlockSegment) {
        super.stopSegment(segment)

        val bukkitLocation = segment.location.toBukkitLocation()
        val block = bukkitLocation.instance?.getBlock(bukkitLocation.position)!!

        player.sendPacket(BlockChangePacket(
            bukkitLocation.position,
            block
        ))
    }
}

