package com.typewritermc.engine.paper.events

import com.github.retrooper.packetevents.protocol.player.InteractionHand
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction
import com.typewritermc.engine.paper.entry.entries.EntityDefinitionEntry
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class AsyncFakeEntityInteract(
    private val player: Player,
    val entityId: Int,
    val hand: InteractionHand,
    val action: InteractAction,
) : PlayerEvent {
    override fun getPlayer(): Player = player
}

class AsyncEntityDefinitionInteract(
    private val player: Player,
    val entityId: Int,
    val definition: EntityDefinitionEntry,
    val hand: InteractionHand,
    val action: InteractAction,
) : PlayerEvent {
    override fun getPlayer(): Player = player
}