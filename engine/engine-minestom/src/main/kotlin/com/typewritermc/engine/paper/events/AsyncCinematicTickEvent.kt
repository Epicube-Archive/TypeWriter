package com.typewritermc.engine.paper.events

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class AsyncCinematicTickEvent(private val player: Player, val frame: Int) : PlayerEvent {
    override fun getPlayer(): Player = player
}