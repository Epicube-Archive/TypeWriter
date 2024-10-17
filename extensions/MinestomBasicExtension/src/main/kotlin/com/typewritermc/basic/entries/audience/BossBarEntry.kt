package com.typewritermc.basic.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.minestom.entry.entries.AudienceDisplay
import com.typewritermc.engine.minestom.entry.entries.AudienceEntry
import com.typewritermc.engine.minestom.entry.entries.TickableDisplay
import com.typewritermc.engine.minestom.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.minestom.utils.asMini
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Entry("boss_bar", "Boss Bar", Colors.GREEN, "carbon:progress-bar")
/**
 * The `BossBarEntry` is a display that shows a bar at the top of the screen.
 *
 * ## How could this be used?
 * This could be used to show objectives in a quest, or to show the progress of a task.
 */
class BossBarEntry(
    override val id: String = "",
    override val name: String = "",
    @Colored
    @Placeholder
    @Help("The title of the boss bar")
    val title: String = "",
    @Help("How filled up the bar is. 0.0 is empty, 1.0 is full.")
    val progress: Double = 1.0,
    @Help("The color of the boss bar")
    val color: BossBar.Color = BossBar.Color.WHITE,
    @Help("If the bossbar has notches")
    val style: BossBar.Overlay = BossBar.Overlay.PROGRESS,
    @Help("Any flags to apply to the boss bar")
    val flags: List<BossBar.Flag> = emptyList(),
) : AudienceEntry {
    override fun display(): AudienceDisplay {
        return BossBarDisplay(title, progress, color, style, flags)
    }
}

class BossBarDisplay(
    private val title: String,
    private val progress: Double,
    private val color: BossBar.Color,
    private val style: BossBar.Overlay,
    private val flags: List<BossBar.Flag>,
) : AudienceDisplay(), TickableDisplay {
    private val bars = ConcurrentHashMap<UUID, BossBar>()

    override fun tick() {
        for ((id, bar) in bars) {
            bar.name(title.parsePlaceholders(id).asMini())
        }
    }

    override fun onPlayerAdd(player: Player) {
        val bar = BossBar.bossBar(
            title.parsePlaceholders(player).asMini(),
            progress.toFloat(),
            color,
            style,
            flags.toSet()
        )
        bars[player.uuid] = bar
        player.showBossBar(bar)
    }

    override fun onPlayerRemove(player: Player) {
        bars.remove(player.uuid)?.let { player.hideBossBar(it) }
    }
}