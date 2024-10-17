package com.typewritermc.engine.paper.adapt

import java.util.*

class OfflinePlayerImpl(
    override val uuid: UUID,
    override val username: String
) : OfflinePlayer
