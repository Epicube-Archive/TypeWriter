package com.typewritermc.engine.paper.events

import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.entries.QuestEntry
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class AsyncTrackedQuestUpdate(
    private val player: Player,
    val from: Ref<QuestEntry>?,
    val to: Ref<QuestEntry>?,
) : PlayerEvent {
    override fun getPlayer(): Player = player
}