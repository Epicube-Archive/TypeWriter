package com.typewritermc.engine.paper.extensions.placeholderapi

import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.adapt.Plugin
import com.typewritermc.engine.paper.entry.PlaceholderEntry
import lirand.api.extensions.server.server
import com.typewritermc.engine.paper.entry.entries.trackedShowingObjectives
import com.typewritermc.engine.paper.entry.quest.trackedQuest
import com.typewritermc.engine.paper.snippets.snippet
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
    return if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
        PlaceholderAPI.setPlaceholders(player, this)
    } else this
}

fun String.parsePlaceholders(player: Player?): String = parsePlaceholders(player as OfflinePlayer?)

fun String.parsePlaceholders(player: Player?): String = "placeholders not implemented yet"

fun String.parsePlaceholders(playerId: UUID): String = parsePlaceholders(server.getOfflinePlayer(playerId))

val String.isPlaceholder: Boolean
    get() {
        return PlaceholderAPI.getPlaceholderPattern().matcher(this).matches()
    }