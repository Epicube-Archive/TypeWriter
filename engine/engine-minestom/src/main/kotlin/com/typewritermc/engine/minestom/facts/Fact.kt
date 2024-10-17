package com.typewritermc.engine.minestom.facts

import com.typewritermc.engine.minestom.entry.entries.GroupId
import java.time.LocalDateTime

data class FactData(val value: Int, val lastUpdate: LocalDateTime = LocalDateTime.now())

data class FactId(
    val entryId: String,
    val groupId: GroupId,
)