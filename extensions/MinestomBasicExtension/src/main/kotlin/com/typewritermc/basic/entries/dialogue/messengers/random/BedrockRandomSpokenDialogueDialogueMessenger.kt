package com.typewritermc.basic.entries.dialogue.messengers.random

import com.typewritermc.basic.entries.dialogue.RandomSpokenDialogueEntry
import com.typewritermc.core.extension.annotations.Messenger
import com.typewritermc.engine.minestom.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.minestom.entry.dialogue.MessengerFilter
import com.typewritermc.engine.minestom.entry.dialogue.MessengerState
import com.typewritermc.engine.minestom.entry.entries.DialogueEntry
import com.typewritermc.engine.minestom.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.minestom.utils.isFloodgate
import com.typewritermc.engine.minestom.utils.legacy
import net.minestom.server.entity.Player

@Messenger(RandomSpokenDialogueEntry::class, priority = 5)
class BedrockRandomSpokenDialogueDialogueMessenger(player: Player, entry: RandomSpokenDialogueEntry) :
    DialogueMessenger<RandomSpokenDialogueEntry>(player, entry) {

    companion object : MessengerFilter {
        override fun filter(player: Player, entry: DialogueEntry): Boolean = player.isFloodgate
    }

    override fun init() {
        super.init()
        val message = entry.messages.randomOrNull() ?: return
        /* no-op */
    }
}