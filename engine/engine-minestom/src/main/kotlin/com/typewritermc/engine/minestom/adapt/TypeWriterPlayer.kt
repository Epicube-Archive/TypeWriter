package com.typewritermc.engine.minestom.adapt

import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.TimeUpdatePacket
import net.minestom.server.network.player.PlayerConnection
import java.util.*

class TypeWriterPlayer(
    override val uuid: UUID,
    override val username: String,
    playerConnection: PlayerConnection
) : Player(uuid, username, playerConnection), OfflinePlayer {

    private var _playerTime: Long = 0
    var playerTime: Long
        get() = _playerTime
        set(value) {
            _playerTime = value
            sendPacket(TimeUpdatePacket(instance.time, _playerTime))
        }

    fun resetPlayerTime() {
        playerTime = 0
    }
}
