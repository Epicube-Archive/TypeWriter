package com.typewritermc.engine.minestom.events

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class ContentEditorStartEvent(
    private val player: Player
) : PlayerEvent {
    override fun getPlayer(): Player = player
}

class ContentEditorEndEvent(
    private val player: Player
) : PlayerEvent {
    override fun getPlayer(): Player = player
}
