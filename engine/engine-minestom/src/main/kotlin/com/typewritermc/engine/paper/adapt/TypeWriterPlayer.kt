package com.typewritermc.engine.paper.adapt

import net.minestom.server.entity.Player
import net.minestom.server.network.player.PlayerConnection
import java.util.*

class TypeWriterPlayer(
    override val uuid: UUID,
    override val username: String,
    playerConnection: PlayerConnection
) : Player(uuid, username, playerConnection), OfflinePlayer {

    var playerTime: Long = 0

    fun resetPlayerTime() {
        playerTime = 0
    }
}
