package com.typewritermc.engine.paper.adapt.event.bukkit

import com.typewritermc.engine.paper.adapt.Location
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.CancellableEvent
import net.minestom.server.event.trait.PlayerEvent

class PlayerJumpEvent(val player: Player, val from: Location, val to: Location, private var cancelled: Boolean) : PlayerEvent, CancellableEvent {
    override fun getPlayer() = player

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}
