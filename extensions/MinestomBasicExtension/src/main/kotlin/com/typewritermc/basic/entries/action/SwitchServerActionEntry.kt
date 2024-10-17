package com.typewritermc.basic.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.Modifier
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.engine.minestom.entry.entries.ActionEntry
import net.minestom.server.entity.Player

@Entry("switch_server_action", "Switches the player to another server", Colors.RED, "fluent:server-link-16-filled")
/**
 * The `Switch Server Action` is an action that switches the player to another server.
 *
 * ## How could this be used?
 *
 * This could be used to switch a player from one server to another when interacting with an entity, maybe clicking a button.
 */
class SwitchServerActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The server the player will connect to.")
    val server: String = "",
): ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)
        player.sendPluginMessage("BungeeCord", "Connect$server")
    }
}