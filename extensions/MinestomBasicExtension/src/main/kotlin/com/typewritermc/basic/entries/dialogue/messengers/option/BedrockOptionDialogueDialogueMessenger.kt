package com.typewritermc.basic.entries.dialogue.messengers.option

import com.typewritermc.engine.minestom.entry.Modifier
import com.typewritermc.core.entries.Ref
import com.typewritermc.basic.entries.dialogue.Option
import com.typewritermc.basic.entries.dialogue.OptionDialogueEntry
import com.typewritermc.core.extension.annotations.Messenger
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.engine.minestom.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.minestom.entry.dialogue.MessengerFilter
import com.typewritermc.engine.minestom.entry.entries.DialogueEntry
import com.typewritermc.engine.minestom.entry.matches
import com.typewritermc.engine.minestom.utils.isFloodgate
import net.minestom.server.entity.Player

@Messenger(OptionDialogueEntry::class, priority = 5)
class BedrockOptionDialogueDialogueMessenger(player: Player, entry: OptionDialogueEntry) :
    DialogueMessenger<OptionDialogueEntry>(player, entry) {

    companion object : MessengerFilter {
        override fun filter(player: Player, entry: DialogueEntry): Boolean = player.isFloodgate
    }

    private var selectedIndex = 0
    private val selected get() = usableOptions[selectedIndex]

    private var usableOptions: List<Option> = emptyList()

    override val triggers: List<Ref<out TriggerableEntry>>
        get() = entry.triggers + selected.triggers

    override val modifiers: List<Modifier>
        get() = entry.modifiers + selected.modifiers


    override fun init() {
        super.init()
        usableOptions = entry.options.filter { it.criteria.matches(player) }
        /* no-op */
    }

    override fun end() {
        // Do nothing as we don't need to resend the messages.
    }
}