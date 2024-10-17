package com.typewritermc.engine.paper.adapt

import com.typewritermc.engine.paper.adapt.event.EventListenerScanner
import com.typewritermc.engine.paper.adapt.event.Listener
import com.typewritermc.engine.paper.utils.findGlobalPlayerByUuid
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.UUID

class Server private constructor() {
    var minecraftServer: MinecraftServer? = null

    companion object {
        val instance:Server by lazy {
            Server()
        }
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
}
