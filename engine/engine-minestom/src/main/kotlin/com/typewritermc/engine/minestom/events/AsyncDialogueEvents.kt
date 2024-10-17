package com.typewritermc.engine.minestom.events

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class AsyncDialogueStartEvent(private val player: Player) : PlayerEvent {
    override fun getPlayer(): Player = player
}

class AsyncDialogueSwitchEvent(private val player: Player) : PlayerEvent {
    override fun getPlayer(): Player = player
}

class AsyncDialogueEndEvent(private val player: Player) : PlayerEvent {
    override fun getPlayer(): Player = player
}