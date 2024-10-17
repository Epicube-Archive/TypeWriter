package com.typewritermc.engine.minestom.content.components

import com.typewritermc.engine.minestom.adapt.event.EventHandler
import com.typewritermc.engine.minestom.adapt.event.Listener
import com.typewritermc.engine.minestom.adapt.event.bukkit.PlayerToggleSneakEvent
import com.typewritermc.engine.minestom.content.ContentMode
import com.typewritermc.engine.minestom.content.inLastContentMode
import com.typewritermc.engine.minestom.entry.entries.SystemTrigger
import com.typewritermc.engine.minestom.entry.triggerFor
import com.typewritermc.engine.minestom.plugin
import com.typewritermc.engine.minestom.utils.asMini
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material
import java.util.*

fun ContentMode.exit(doubleShiftExits: Boolean = false) = +ExitComponent(doubleShiftExits)
class ExitComponent(
    private val doubleShiftExits: Boolean,
) : ItemComponent, com.typewritermc.engine.minestom.adapt.event.Listener {
    private var playerId: UUID? = null
    private var lastShift = 0L

    override suspend fun initialize(player: Player) {
        super.initialize(player)
        if (!doubleShiftExits) return
        plugin.registerEvents(this)
        playerId = player.uuid
    }

    @com.typewritermc.engine.minestom.adapt.event.EventHandler
    private fun onShift(event: PlayerToggleSneakEvent) {
        if (event.player.uuid != playerId) return
        // Only count shifting down
        if (!event.isSneaking) return
        if (System.currentTimeMillis() - lastShift < 500) {
            SystemTrigger.CONTENT_POP triggerFor event.player
        }
        lastShift = System.currentTimeMillis()
    }

    override suspend fun dispose(player: Player) {
        super.dispose(player)
        unregister()
    }

    override fun item(player: Player): Pair<Int, IntractableItem> {
        val sneakingLine = if (doubleShiftExits) {
            "<line> <gray>Double shift to exit"
        } else {
            ""
        }
        val item = if (player.inLastContentMode) {
            ItemStack.of(Material.BARRIER)
                .withCustomName("<red><bold>Exit Editor".asMini())
                .withLore("""
                    |
                    |<line> <gray>Click to exit the editor.
                    |$sneakingLine
                """.trimMargin().asMini())
        } else {
            ItemStack.of(Material.END_CRYSTAL)
                .withCustomName("<yellow><bold>Previous Editor".asMini())
                .withLore("""
                    |
                    |<line> <gray>Click to go back to the previous editor.
                    |$sneakingLine
                """.trimMargin().asMini())
        }

        return 8 to item {
            SystemTrigger.CONTENT_POP triggerFor player
        }
    }
}