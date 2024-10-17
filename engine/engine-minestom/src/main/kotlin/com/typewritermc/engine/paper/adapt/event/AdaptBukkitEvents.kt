package com.typewritermc.engine.paper.adapt.event

import com.typewritermc.engine.paper.adapt.event.bukkit.PlayerInteractEvent
import com.typewritermc.engine.paper.adapt.event.bukkit.PlayerToggleSneakEvent
import com.typewritermc.engine.paper.utils.callEvent
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.*

object AdaptBukkitEvents {
    fun register() {
        val handler = MinecraftServer.getGlobalEventHandler()
        handler.addListener(PlayerStartSneakingEvent::class.java) { PlayerToggleSneakEvent(it.player, true).callEvent() }
        handler.addListener(PlayerStopSneakingEvent::class.java) { PlayerToggleSneakEvent(it.player, false).callEvent() }

        handler.addListener(PlayerBlockInteractEvent::class.java) {
            val event = PlayerInteractEvent(it.player, it.hand, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, it.isCancelled)
            handler.call(event)
            it.isCancelled = event.isCancelled
        }

        handler.addListener(PlayerStartDiggingEvent::class.java) {
            val event = PlayerInteractEvent(it.player, it.player.playerMeta.activeHand, PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, it.isCancelled)
            handler.call(event)
            it.isCancelled = event.isCancelled
        }

        handler.addListener(PlayerUseItemEvent::class.java) {
            val event = PlayerInteractEvent(it.player, it.hand, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, it.isCancelled)
            handler.call(event)
            it.isCancelled = event.isCancelled
        }

        handler.addListener(PlayerHandAnimationEvent::class.java) {
            val event = PlayerInteractEvent(it.player, it.hand, PlayerInteractEvent.Action.LEFT_CLICK_AIR, it.isCancelled)
            handler.call(event)
            it.isCancelled = event.isCancelled
        }
    }
}
