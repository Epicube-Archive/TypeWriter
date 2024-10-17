package com.typewritermc.engine.minestom.adapt

import java.util.*

class OfflinePlayerImpl(
    override val uuid: UUID,
    override val username: String
) : OfflinePlayer
