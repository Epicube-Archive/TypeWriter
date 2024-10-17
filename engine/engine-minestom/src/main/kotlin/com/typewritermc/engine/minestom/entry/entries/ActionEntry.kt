package com.typewritermc.engine.minestom.entry.entries

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.engine.minestom.entry.triggerEntriesFor
import com.typewritermc.engine.minestom.facts.FactDatabase
import net.minestom.server.entity.Player
import org.koin.java.KoinJavaComponent.get

@Tags("action")
interface ActionEntry : TriggerableEntry {
    fun execute(player: Player) {
        val factDatabase: FactDatabase = get(FactDatabase::class.java)
        factDatabase.modify(player, modifiers)
    }
}

@Tags("custom_triggering_action")
interface CustomTriggeringActionEntry : ActionEntry {
    // Disable the normal triggers. So that the action can manually trigger the next actions.
    override val triggers: List<Ref<TriggerableEntry>>
        get() = emptyList()

    @Help("The entries that will be fired after this entry.")
    val customTriggers: List<Ref<TriggerableEntry>>

    fun Player.triggerCustomTriggers() {
        customTriggers triggerEntriesFor this
    }
}