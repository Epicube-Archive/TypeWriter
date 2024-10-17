package com.typewritermc.basic.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.Modifier
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.engine.minestom.entry.entries.ActionEntry
import com.typewritermc.engine.minestom.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.minestom.utils.ThreadType.SYNC
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

@Entry("console_run_command", "Run command from console", Colors.RED, "mingcute:terminal-fill")
/**
 * The Console Command Action is an action that sends a command to the server console. This action provides you with the ability to execute console commands on the server in response to specific events.
 *
 * ## How could this be used?
 *
 * This action can be useful in a variety of situations. You can use it to perform administrative tasks, such as sending a message to all players on the server, or to automate server tasks, such as setting the time of day or weather conditions. The possibilities are endless!
 */
class ConsoleCommandActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Placeholder
    @MultiLine
    @Help("Every line is a different command. Commands should not be prefixed with <code>/</code>.")
    private val command: String = "",
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)
        // Run in the main thread
        if (command.isBlank()) return
        SYNC.launch {
            val commands = command.parsePlaceholders(player).lines()
            for (cmd in commands) {
                MinecraftServer.getCommandManager().executeServerCommand(cmd)
            }
        }
    }
}