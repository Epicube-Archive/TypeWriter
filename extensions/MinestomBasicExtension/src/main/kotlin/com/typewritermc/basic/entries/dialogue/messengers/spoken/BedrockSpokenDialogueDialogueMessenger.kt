package com.typewritermc.basic.entries.dialogue.messengers.spoken

import com.typewritermc.basic.entries.dialogue.SpokenDialogueEntry
import com.typewritermc.core.extension.annotations.Messenger
import com.typewritermc.engine.minestom.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.minestom.entry.dialogue.MessengerFilter
import com.typewritermc.engine.minestom.entry.entries.DialogueEntry
import com.typewritermc.engine.minestom.utils.isFloodgate
import net.minestom.server.entity.Player

@Messenger(SpokenDialogueEntry::class, priority = 5)
class BedrockSpokenDialogueDialogueMessenger(player: Player, entry: SpokenDialogueEntry) :
    DialogueMessenger<SpokenDialogueEntry>(player, entry) {

    companion object : MessengerFilter {
        override fun filter(player: Player, entry: DialogueEntry): Boolean = player.isFloodgate
    }

    override fun init() {
        super.init()
        /* no-op */
    }

    override fun end() {
        // Do nothing as we don't need to resend the messages.
    }
}