package com.typewritermc.engine.minestom.adapt.event.bukkit

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class PlayerToggleSneakEvent(val player: Player, val isSneaking: Boolean) : PlayerEvent {
    override fun getPlayer() = player
}
