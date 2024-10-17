package com.typewritermc.engine.minestom.entry.entries

import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.minestom.entry.PlaceholderEntry
import com.typewritermc.engine.minestom.entry.StaticEntry
import net.minestom.server.entity.Player

@Tags("sound_id")
interface SoundIdEntry : StaticEntry, PlaceholderEntry {
    val soundId: String

    override fun display(player: Player?): String? = soundId
}

@Tags("sound_source")
interface SoundSourceEntry : StaticEntry {
    fun getEmitter(player: Player): SoundEmitter
}

class SoundEmitter(val entityId: Int)