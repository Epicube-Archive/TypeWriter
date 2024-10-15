package com.typewritermc.engine.paper.content

import net.minestom.server.entity.Player

interface ContentComponent {
    suspend fun initialize(player: Player)
    suspend fun tick(player: Player)
    suspend fun dispose(player: Player)
}