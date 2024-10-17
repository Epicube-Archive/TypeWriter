package com.typewritermc.basic.entries.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.cinematic.SimpleCinematicAction
import com.typewritermc.engine.minestom.entry.entries.CinematicAction
import com.typewritermc.engine.minestom.entry.entries.PrimaryCinematicEntry
import com.typewritermc.engine.minestom.entry.entries.Segment
import com.typewritermc.engine.minestom.extensions.packetevents.toPacketItem
import com.typewritermc.engine.minestom.utils.unClickable
import net.kyori.adventure.text.Component
import net.minestom.server.entity.EquipmentSlot
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket

@Entry("pumpkin_hat_cinematic", "Show a pumpkin hat during a cinematic", Colors.CYAN, "mingcute:hat-fill")
/**
 * The `Pumpkin Hat Cinematic` is a cinematic that shows a pumpkin hat on the player's head.
 *
 * ## How could this be used?
 * When you have a resource pack, you can re-texture the pumpkin overlay to make it look like cinematic black bars.
 */
class PumpkinHatCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(icon = "mingcute:hat-fill")
    val segments: List<PumpkinHatSegment> = emptyList(),
) : PrimaryCinematicEntry {
    override fun create(player: Player): CinematicAction {
        return PumpkinHatCinematicAction(
            player,
            this,
        )
    }
}

data class PumpkinHatSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
) : Segment

class PumpkinHatCinematicAction(
    private val player: Player,
    entry: PumpkinHatCinematicEntry,
) : SimpleCinematicAction<PumpkinHatSegment>() {
    override val segments: List<PumpkinHatSegment> = entry.segments

    override suspend fun startSegment(segment: PumpkinHatSegment) {
        super.startSegment(segment)

        player.sendPacket(EntityEquipmentPacket(
            player.entityId,
            mapOf(
                Pair(EquipmentSlot.HELMET, ItemStack.of(Material.CARVED_PUMPKIN)
                    .withCustomName(Component.text(" "))
                    .unClickable())
            )
        ))
    }

    override suspend fun stopSegment(segment: PumpkinHatSegment) {
        super.stopSegment(segment)

        player.sendPacket(EntityEquipmentPacket(
            player.entityId,
            mapOf(
                Pair(EquipmentSlot.HELMET, player.inventory.helmet)
            )
        ))
    }
}