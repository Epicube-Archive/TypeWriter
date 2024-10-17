package com.typewritermc.engine.minestom.adapt.event.bukkit

import net.minestom.server.entity.Player
import net.minestom.server.entity.Player.Hand
import net.minestom.server.event.trait.CancellableEvent
import net.minestom.server.event.trait.PlayerEvent

class PlayerInteractEvent(val player: Player, val hand: Hand, val action: Action, private var cancelled: Boolean) : PlayerEvent, CancellableEvent {
    override fun getPlayer() = player

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    enum class Action {
        LEFT_CLICK_AIR,
        LEFT_CLICK_BLOCK,
        RIGHT_CLICK_AIR,
        RIGHT_CLICK_BLOCK
    }
}
