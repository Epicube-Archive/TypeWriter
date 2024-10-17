package com.typewritermc.basic.entries.dialogue.messengers.random

import com.typewritermc.basic.entries.dialogue.RandomMessageDialogueEntry
import com.typewritermc.basic.entries.dialogue.messengers.message.sendMessageDialogue
import com.typewritermc.core.extension.annotations.Messenger
import com.typewritermc.engine.minestom.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.minestom.entry.dialogue.MessengerFilter
import com.typewritermc.engine.minestom.entry.dialogue.MessengerState
import com.typewritermc.engine.minestom.entry.dialogue.TickContext
import com.typewritermc.engine.minestom.entry.entries.DialogueEntry
import net.minestom.server.entity.Player
import java.time.Duration

@Messenger(RandomMessageDialogueEntry::class)
class RandomMessageDialogueDialogueMessenger(player: Player, entry: RandomMessageDialogueEntry) :
    DialogueMessenger<RandomMessageDialogueEntry>(player, entry) {

    companion object : MessengerFilter {
        override fun filter(player: Player, entry: DialogueEntry): Boolean = true
    }

    override fun tick(context: TickContext) {
        super.tick(context)
        if (state != MessengerState.RUNNING) return
        state = MessengerState.FINISHED
        val message = entry.messages.randomOrNull() ?: return
        player.sendMessageDialogue(message, entry.speakerDisplayName)
    }
}