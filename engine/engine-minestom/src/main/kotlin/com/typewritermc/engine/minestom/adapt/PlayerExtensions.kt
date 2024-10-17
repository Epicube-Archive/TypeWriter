package com.typewritermc.engine.minestom.adapt

import net.minestom.server.entity.Player

fun Player.asTypeWriterPlayer(): TypeWriterPlayer {
    return this as TypeWriterPlayer
}