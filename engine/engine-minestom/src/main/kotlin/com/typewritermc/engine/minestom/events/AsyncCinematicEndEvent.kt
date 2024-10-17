package com.typewritermc.engine.minestom.events

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class AsyncCinematicEndEvent(private val player: Player, val frame: Int, val pageId: String) : PlayerEvent {
    override fun getPlayer(): Player = player
}