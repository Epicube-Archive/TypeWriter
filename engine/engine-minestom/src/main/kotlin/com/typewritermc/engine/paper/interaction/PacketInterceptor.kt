package com.typewritermc.engine.paper.interaction

import com.typewritermc.engine.paper.adapt.event.EventHandler
import com.typewritermc.engine.paper.adapt.event.Listener
import lirand.api.extensions.server.server
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.event.player.PlayerPacketOutEvent
import org.koin.java.KoinJavaComponent.get
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// TODO: this class needs lots of changes
class PacketInterceptor : Listener {
    private val blockers = ConcurrentHashMap<UUID, PlayerPacketInterceptor>()

    fun initialize() {
        server.registerEvents(this)
    }

    @EventHandler
    fun onPacketReceive(event: PlayerPacketEvent) {
        val player = event.player
        val interceptor = blockers[player.uuid] ?: return
        interceptor.trigger(event)
    }

    @EventHandler
    fun onPacketSend(event: PlayerPacketOutEvent) {
        val player = event.player
        val interceptor = blockers[player.uuid] ?: return
        interceptor.trigger(event)
    }

    fun interceptPacket(player: UUID, interception: PacketInterception): PacketInterceptionSubscription {
        val subscription = PacketInterceptionSubscription()
        blockers.compute(player) { _, interceptor ->
            val newInterceptor = interceptor ?: PlayerPacketInterceptor()
            newInterceptor.intercept(subscription, interception)
            newInterceptor
        }
        return subscription
    }

    fun cancel(player: UUID, subscription: PacketInterceptionSubscription) {
        blockers.compute(player) { _, blocker ->
            val newBlocker = blocker ?: return@compute null
            if (newBlocker.cancel(subscription)) null else newBlocker
        }
    }

    fun cancel(player: UUID, bundle: InterceptionBundle) {
        blockers.compute(player) { _, blocker ->
            val newBlocker = blocker ?: return@compute null
            if (newBlocker.cancel(bundle)) null else newBlocker
        }
    }

    fun shutdown() {
        blockers.clear()
    }
}

private data class PlayerPacketInterceptor(
    val interceptions: ConcurrentHashMap<PacketInterceptionSubscription, PacketInterception> = ConcurrentHashMap()
) {
    fun intercept(
        subscription: PacketInterceptionSubscription,
        interception: PacketInterception
    ): PacketInterceptionSubscription {
        interceptions[subscription] = interception
        return subscription
    }

    fun cancel(subscription: PacketInterceptionSubscription): Boolean {
        interceptions.remove(subscription)
        return interceptions.isEmpty()
    }

    fun cancel(bundle: InterceptionBundle): Boolean {
        bundle.subscriptions.forEach { cancel(it) }
        return interceptions.isEmpty()
    }

    fun trigger(event: ProtocolPacketEvent) {
        interceptions.values
            .asSequence()
            .filter { it.type == event.packetType }
            .forEach { it.onIntercept(event) }
    }
}

data class PacketInterceptionSubscription(
    val subscriptionId: UUID = UUID.randomUUID(),
)

interface PacketInterception {
    val type: PacketTypeCommon
    fun onIntercept(event: ProtocolPacketEvent)
}

class PacketBlocker(
    override val type: PacketTypeCommon,
) : PacketInterception {
    override fun onIntercept(event: ProtocolPacketEvent) {
        event.isCancelled = true
    }
}

class CustomPacketReceiveInterception(
    override val type: PacketTypeCommon,
    private val intercept: (PacketReceiveEvent) -> Unit
) : PacketInterception {
    override fun onIntercept(event: ProtocolPacketEvent) {
        if (event !is PacketReceiveEvent) return
        intercept(event)
    }
}

class CustomPacketSendInterception(
    override val type: PacketTypeCommon,
    private val intercept: (PacketSendEvent) -> Unit
) : PacketInterception {
    override fun onIntercept(event: ProtocolPacketEvent) {
        if (event !is PacketSendEvent) return
        intercept(event)
    }
}

fun Player.interceptPackets(block: InterceptionBundle.() -> Unit): InterceptionBundle {
    val bundle = InterceptionBundle(uniqueId)
    block(bundle)
    return bundle
}

class InterceptionBundle(private val playerId: UUID) {
    internal val subscriptions = mutableListOf<PacketInterceptionSubscription>()

    private fun intercept(interception: PacketInterception) {
        val subscription = get<PacketInterceptor>(PacketInterceptor::class.java).interceptPacket(playerId, interception)
        subscriptions.add(subscription)
    }

    operator fun PacketTypeCommon.not() {
        intercept(PacketBlocker(this))
    }

    operator fun ClientBoundPacket.invoke(onIntercept: (PacketSendEvent) -> Unit) {
        if (this !is PacketTypeCommon) return
        intercept(CustomPacketSendInterception(this, onIntercept))
    }

    operator fun ServerBoundPacket.invoke(onIntercept: (PacketReceiveEvent) -> Unit) {
        if (this !is PacketTypeCommon) return
        intercept(CustomPacketReceiveInterception(this, onIntercept))
    }

    fun cancel() {
        val interceptor = get<PacketInterceptor>(PacketInterceptor::class.java)
        interceptor.cancel(playerId, this)
    }
}
