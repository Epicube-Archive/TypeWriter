package com.typewritermc.engine.paper.adapt

import com.typewritermc.engine.paper.utils.findGlobalPlayerByUuid
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.UUID

class Server private constructor() {
    companion object {
        val instance:Server by lazy {
            Server()
        }
    }

    fun getPlayer(uuid: UUID): Player? {
        return MinecraftServer.getInstanceManager().findGlobalPlayerByUuid(uuid)
    }
}
