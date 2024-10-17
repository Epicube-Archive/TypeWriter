package com.typewritermc.engine.minestom.extensions.placeholderapi

import com.typewritermc.core.entries.Query
import com.typewritermc.engine.minestom.adapt.OfflinePlayer
import com.typewritermc.engine.minestom.adapt.Plugin
import com.typewritermc.engine.minestom.entry.PlaceholderEntry
import lirand.api.extensions.server.server
import com.typewritermc.engine.minestom.entry.entries.trackedShowingObjectives
import com.typewritermc.engine.minestom.entry.quest.trackedQuest
import com.typewritermc.engine.minestom.snippets.snippet
import net.minestom.server.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*


private val noneTracked by snippet("quest.tracked.none", "<gray>None tracked</gray>")

object PlaceholderExpansion : KoinComponent {
    private val plugin: Plugin by inject()
    fun getIdentifier(): String = "typewriter"
    fun getAuthor(): String = "gabber235"
    fun getVersion(): String = plugin.version
    fun persist(): Boolean = true

    fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (params == "tracked_quest") {
            if (player == null) return null
            return player.trackedQuest()?.get()?.display(player) ?: noneTracked
        }

        if (params == "tracked_objectives") {
            if (player == null) return null
            return player.trackedShowingObjectives().joinToString(", ") { it.display(player) }
                .ifBlank { noneTracked }
        }

        val entry: PlaceholderEntry = Query.findById(params) ?: Query.findByName(params) ?: return null
        return entry.display(player)
    }
}

// TODO: implement placeholder support
fun String.parsePlaceholders(player: OfflinePlayer?): String {
    return this // no support for PAPI
}

fun String.parsePlaceholders(player: Player?): String = parsePlaceholders(player as OfflinePlayer?)

fun String.parsePlaceholders(playerId: UUID): String = parsePlaceholders(server.getOfflinePlayer(playerId))

val String.isPlaceholder: Boolean get() {
    return false // no support for PAPI
}