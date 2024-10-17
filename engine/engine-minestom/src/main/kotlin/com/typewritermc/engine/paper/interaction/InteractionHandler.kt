package com.typewritermc.engine.paper.interaction

import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.adapt.event.EventHandler
import com.typewritermc.engine.paper.adapt.event.Listener
import com.typewritermc.engine.paper.entry.entries.CustomCommandEntry
import com.typewritermc.engine.paper.entry.entries.Event
import com.typewritermc.engine.paper.entry.entries.EventTrigger
import com.typewritermc.engine.paper.entry.entries.SystemTrigger.DIALOGUE_END
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.ThreadType.DISPATCHERS_ASYNC
import kotlinx.coroutines.runBlocking
import lirand.api.extensions.server.onlinePlayers
import lirand.api.extensions.server.registerEvents
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerCommandEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import org.koin.core.component.KoinComponent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal const val TICK_MS = 50L
// This is the most magic value I have ever seen.
internal const val AVERAGE_SCHEDULING_DELAY_MS = 5L

class InteractionHandler : Listener, KoinComponent {
    private val interactions = ConcurrentHashMap<UUID, Interaction>()

    val Player.interaction: Interaction?
        get() = interactions[uuid]


    /** Some triggers start dialogue. Though we don't want to trigger the starting of dialogue multiple times,
     * we need to check if the player is already in a dialogue.
     *
     * @param player The player who interacted
     * @param initialTriggers The trigger that should be fired after the interaction started
     * @param continueTrigger The trigger that should be fired if the interaction is already active
     */
    fun startDialogueWithOrTriggerEvent(
        player: Player,
        initialTriggers: List<EventTrigger>,
        continueTrigger: EventTrigger? = null
    ) {
        val interaction = player.interaction ?: return
        if (interaction.hasDialogue) {
            if (continueTrigger != null) {
                triggerEvent(Event(player, continueTrigger))
            }
        } else {
            triggerEvent(Event(player, initialTriggers))
        }
    }

    /**
     * Triggers a list of actions.
     *
     * @param player The player who interacted
     * @param triggers A list of triggers that should be fired.
     */
    fun triggerActions(player: Player, triggers: List<EventTrigger>) {
        triggerEvent(Event(player, triggers))
    }

    /**
     * Forces an event to be executed.
     * This will bypass the event queue and execute the event immediately.
     * This is useful for events that need to be executed immediately.
     * **This should only be used sparingly.**
     *
     * @param player The player who interacted
     * @param triggers The trigger that should be fired.
     */
    suspend fun forceTriggerActions(player: Player, triggers: List<EventTrigger>) {
        player.interaction?.forceEvent(Event(player, triggers))
    }


    private fun triggerEvent(event: Event) {
        // If the event is empty, we don't need to do anything
        if (event.triggers.isEmpty()) return

        DISPATCHERS_ASYNC.launch {
            try {
                event.player.interaction?.addToSchedule(event)
            } catch (e: Exception) {
                logger.severe("An error occurred while handling event ${event}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun initialize() {
        plugin.registerEvents(this)
    }

    // When a player joins the server, we need to create an interaction for them.
    @EventHandler(priority = -5)
    fun onPlayerJoin(event: PlayerSpawnEvent) {
        val interaction = Interaction(event.player)
        interactions[event.player.uuid] = interaction
        interaction.setup()
    }

    // When a player leaves the server, we need to end the interaction.
    @EventHandler(priority = 10)
    fun onPlayerQuit(event: PlayerDisconnectEvent) {
        runBlocking {
            interactions.remove(event.player.uuid)?.end()
        }
    }

    fun load() {
        interactions.putAll(onlinePlayers.map { it.uuid to Interaction(it) })
        interactions.forEach { (_, interaction) ->
            interaction.setup()
        }
    }

    suspend fun unload() {
        interactions.forEach { (_, interaction) ->
            interaction.end()
        }
        interactions.clear()
    }

    // When a player tries to execute a command, we need to end the dialogue.
    @EventHandler(priority = 10)
    fun onPlayerCommandPreprocess(event: PlayerCommandEvent) {
        val command = event.command.removePrefix("/")
        // We don't want to end the dialogue if the player is running a typewriter command
        if (command.startsWith("typewriter")) return
        if (command.startsWith("tw")) return


        // If this is a custom command, we don't want to end the dialogue
        val entry = Query.firstWhere<CustomCommandEntry> {
            command.startsWith(it.command)
        }
        if (entry != null) return

        // If no dialogue is active, we don't want to cancel any who would be triggered by this.
        val inDialogue = event.player.interaction?.hasDialogue ?: false
        if (!inDialogue) return

        DIALOGUE_END triggerFor event.player
    }

    suspend fun shutdown() {
        interactions.forEach { (_, interaction) ->
            interaction.end()
        }
        interactions.clear()
    }
}