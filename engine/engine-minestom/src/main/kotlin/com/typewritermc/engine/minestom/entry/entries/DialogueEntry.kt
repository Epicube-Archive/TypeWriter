package com.typewritermc.engine.minestom.entry.entries

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.minestom.entry.TriggerableEntry

@Tags("dialogue")
interface DialogueEntry : TriggerableEntry {
    @Help("The speaker of the dialogue")
    val speaker: Ref<SpeakerEntry>

    val speakerDisplayName: String
        get() = speaker.get()?.displayName ?: ""
}

