package com.typewritermc.engine.minestom.adapt

import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.TimeUpdatePacket
import net.minestom.server.network.player.PlayerConnection
import java.util.*

class TypeWriterPlayer(
    uniqueId: UUID,
    name: String,
    playerConnection: PlayerConnection
) : Player(uniqueId, name, playerConnection), OfflinePlayer {

    override val uniqueId: UUID
        get() = super.getUuid()

    override val name: String
        get() = super.getUsername()

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
