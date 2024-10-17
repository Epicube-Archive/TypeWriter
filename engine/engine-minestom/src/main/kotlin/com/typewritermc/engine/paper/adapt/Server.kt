package com.typewritermc.engine.paper.adapt

import com.typewritermc.engine.paper.adapt.event.EventListenerScanner
import com.typewritermc.engine.paper.adapt.event.Listener
import com.typewritermc.engine.paper.utils.findGlobalPlayerByUuid
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import java.nio.file.Path
import java.util.UUID

class Server private constructor() {
    private val offlinePlayerLoader = GsonConfigurationLoader.builder().path(Path.of("offline_players.json")).build()
    private val offlinePlayerStorage = offlinePlayerLoader.load()

    var minecraftServer: MinecraftServer? = null

    companion object {
        val instance:Server by lazy {
            Server()
        }
    }

    val isPrimaryThread: Boolean get() {
        return TODO("thread check")
    }

    fun getPlayer(uuid: UUID): Player? {
        return MinecraftServer.getInstanceManager().findGlobalPlayerByUuid(uuid)
    }

    fun registerEvents(vararg listeners: Listener) = listeners.forEach {
        EventListenerScanner.registerListeners(MinecraftServer.getGlobalEventHandler(), it)
    }

    fun registerSuspendingEvents(vararg listeners: Listener) = listeners.forEach {
        EventListenerScanner.registerListeners(MinecraftServer.getGlobalEventHandler(), it, true)
    }

    fun getOfflinePlayer(playerId: UUID): OfflinePlayer {
        val node = offlinePlayerStorage.node(playerId.toString())
        if(node.virtual()) {
            val onlinePlayer = MinecraftServer.getInstanceManager().findGlobalPlayerByUuid(playerId);
            if(onlinePlayer is OfflinePlayer) {
                return onlinePlayer
            }
            return OfflinePlayerImpl(playerId, "UnknownPlayer")
        }
        return node.get(OfflinePlayer::class.java)!!
    }
}
