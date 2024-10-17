package com.typewritermc.engine.paper.interaction

import com.typewritermc.engine.paper.adapt.event.EventHandler
import com.typewritermc.engine.paper.adapt.event.Listener
import com.typewritermc.engine.paper.adapt.event.packet.WrappedPacketEvent
import lirand.api.extensions.server.server
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.event.player.PlayerPacketOutEvent
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.server.ServerPacket
import org.koin.java.KoinJavaComponent.get
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

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
        interceptor.trigger(WrappedPacketEvent(event))
    }

    @EventHandler
    fun onPacketSend(event: PlayerPacketOutEvent) {
        val player = event.player
        val interceptor = blockers[player.uuid] ?: return
        interceptor.trigger(WrappedPacketEvent(event))
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

    fun trigger(event: WrappedPacketEvent) {
        interceptions.values
            .asSequence()
            .filter { it.type.isInstance(event.packet()) }
            .forEach { it.onIntercept(event) }
    }
}

data class PacketInterceptionSubscription(
    val subscriptionId: UUID = UUID.randomUUID(),
)

interface PacketInterception {
    val type: KClass<*>
    fun onIntercept(event: WrappedPacketEvent)
}

class PacketBlocker(
    override val type: KClass<*>,
) : PacketInterception {
    override fun onIntercept(event: WrappedPacketEvent) {
        event.isCancelled = true
    }
}

class CustomPacketReceiveInterception(
    override val type: KClass<*>,
    private val intercept: (PlayerPacketEvent) -> Unit
) : PacketInterception {
    override fun onIntercept(event: WrappedPacketEvent) {
        if (event.isServer) return
        intercept(event.asClient())
    }
}

class CustomPacketSendInterception(
    override val type: KClass<*>,
    private val intercept: (PlayerPacketOutEvent) -> Unit
) : PacketInterception {
    override fun onIntercept(event: WrappedPacketEvent) {
        if (event.isClient) return
        intercept(event.asServer())
    }
}

fun Player.interceptPackets(block: InterceptionBundle.() -> Unit): InterceptionBundle {
    val bundle = InterceptionBundle(uuid)
    block(bundle)
    return bundle
}

class InterceptionBundle(private val playerId: UUID) {
    internal val subscriptions = mutableListOf<PacketInterceptionSubscription>()

    private fun intercept(interception: PacketInterception) {
        val subscription = get<PacketInterceptor>(PacketInterceptor::class.java).interceptPacket(playerId, interception)
        subscriptions.add(subscription)
    }

    operator fun KClass<Any>.not() {
        intercept(PacketBlocker(this))
    }

    operator fun <T : ServerPacket> T.invoke(onIntercept: (PlayerPacketOutEvent) -> Unit) {
        intercept(CustomPacketSendInterception(this::class, onIntercept))
    }

    operator fun <T : ClientPacket> T.invoke(onIntercept: (PlayerPacketEvent) -> Unit) {
        intercept(CustomPacketReceiveInterception(this::class, onIntercept))
    }

    fun cancel() {
        val interceptor = get<PacketInterceptor>(PacketInterceptor::class.java)
        interceptor.cancel(playerId, this)
    }
}
