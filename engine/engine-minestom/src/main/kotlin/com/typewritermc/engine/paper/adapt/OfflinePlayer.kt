package com.typewritermc.engine.paper.adapt

import java.util.UUID

interface OfflinePlayer {
    val uuid: UUID
    val username: String
}
