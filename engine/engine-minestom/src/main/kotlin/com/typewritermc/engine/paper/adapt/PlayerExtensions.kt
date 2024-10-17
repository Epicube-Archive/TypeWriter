package com.typewritermc.engine.paper.adapt

import net.minestom.server.entity.Player

fun Player.asTypeWriterPlayer(): TypeWriterPlayer {
    return this as TypeWriterPlayer
}