package com.typewritermc.engine.paper.events

import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.entries.QuestEntry
import com.typewritermc.engine.paper.entry.quest.QuestStatus
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class AsyncQuestStatusUpdate(
    private val player: Player,
    val quest: Ref<QuestEntry>,
    val from: QuestStatus,
    val to: QuestStatus,
) : PlayerEvent {
    override fun getPlayer(): Player = player
}