package com.typewritermc.engine.paper.events

import com.typewritermc.engine.paper.entry.entries.EntityDefinitionEntry
import net.minestom.server.entity.Player
import net.minestom.server.entity.Player.Hand
import net.minestom.server.event.trait.PlayerEvent
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket

class AsyncFakeEntityInteract(
    private val player: Player,
    val entityId: Int,
    val hand: Hand,
    val action: ClientInteractEntityPacket.Type,
) : PlayerEvent {
    override fun getPlayer(): Player = player
}

class AsyncEntityDefinitionInteract(
    private val player: Player,
    val entityId: Int,
    val definition: EntityDefinitionEntry,
    val hand: Hand,
    val action: ClientInteractEntityPacket.Type,
) : PlayerEvent {
    override fun getPlayer(): Player = player
}