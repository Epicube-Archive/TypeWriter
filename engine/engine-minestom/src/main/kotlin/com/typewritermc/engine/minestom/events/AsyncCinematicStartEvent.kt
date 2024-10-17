package com.typewritermc.engine.minestom.events

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class AsyncCinematicStartEvent(private val player: Player, val pageId: String) : PlayerEvent {
    override fun getPlayer(): Player = player
}
