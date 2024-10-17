package com.typewritermc.engine.paper.interaction

import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.typewritermc.engine.paper.adapt.event.EventHandler
import com.typewritermc.engine.paper.adapt.event.Listener
import lirand.api.extensions.server.server
import com.typewritermc.engine.paper.plugin
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.event.player.PlayerPacketOutEvent
import net.minestom.server.network.packet.server.play.ActionBarPacket
import net.minestom.server.network.packet.server.play.SystemChatPacket
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.java.KoinJavaComponent.get
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class ActionBarBlockerHandler : Listener {
    fun initialize() {
        server.pluginManager.registerSuspendingEvents(this, plugin)
    }

    private val blockers = mutableMapOf<UUID, ActionBarBlocker>()

    @EventHandler
    fun onPacketSend(event: PlayerPacketOutEvent) {
        val blocker = blockers[event.player.uuid] ?: return
        val packet = event.packet

        val component = when (packet) {
            is SystemChatPacket -> {
                if (!packet.overlay) return
                packet.message
            }

            is ActionBarPacket -> {
                packet.text
            }

            else -> return
        }

        if (blocker.isMessageAccepted(component)) return
        event.isCancelled = true
    }

    fun acceptMessage(player: Player, message: Component) {
        val blocker = blockers[player.uuid] ?: return
        blocker.acceptMessage(message)
    }

    fun enable(player: Player) {
        if (player.uuid in blockers) return
        blockers[player.uuid] = ActionBarBlocker()
    }

    fun disable(player: Player) {
        blockers.remove(player.uuid)
    }

    @EventHandler(priority = 10)
    fun onQuit(event: PlayerDisconnectEvent) {
        blockers.remove(event.player.uuid)
    }

    fun shutdown() {
    }
}

fun Player.startBlockingActionBar() {
    get<ActionBarBlockerHandler>(ActionBarBlockerHandler::class.java).enable(this)
}

fun Player.stopBlockingActionBar() {
    get<ActionBarBlockerHandler>(ActionBarBlockerHandler::class.java).disable(this)
}

fun Player.acceptActionBarMessage(message: Component) {
    get<ActionBarBlockerHandler>(ActionBarBlockerHandler::class.java).acceptMessage(this, message)
}

/**
 * Keep track of the last 20 accepted messages that are allowed to be sent to the player.
 */
class ActionBarBlocker {
    private val acceptedMessages = ConcurrentLinkedQueue<Component>()

    fun acceptMessage(message: Component) {
        if (isMessageAccepted(message)) return
        acceptedMessages.add(message)
        while (acceptedMessages.size > 20) {
            acceptedMessages.poll()
        }
    }

    fun isMessageAccepted(message: Component): Boolean {
        return acceptedMessages.contains(message)
    }
}