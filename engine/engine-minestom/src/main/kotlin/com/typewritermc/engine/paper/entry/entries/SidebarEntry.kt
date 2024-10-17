package com.typewritermc.engine.paper.entry.entries

import com.typewritermc.core.entries.PriorityEntry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.priority
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.adapt.ObjectiveMode
import com.typewritermc.engine.paper.entry.*
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.asMini
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket
import net.minestom.server.network.packet.server.play.ResetScorePacket
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket
import net.minestom.server.network.packet.server.play.UpdateScorePacket
import net.minestom.server.scoreboard.Sidebar
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

private const val MAX_LINES = 15
private const val SCOREBOARD_OBJECTIVE = "typewriter"

@Tags("sidebar")
interface SidebarEntry : AudienceFilterEntry, PlaceholderEntry, PriorityEntry {
    @Help("The title of the sidebar")
    @Colored
    @Placeholder
    val title: String

    override fun display(player: Player?): String? = title.parsePlaceholders(player)

    override fun display(): AudienceFilter = SidebarFilter(ref()) { player ->
        PlayerSidebarDisplay(player, SidebarFilter::class, ref())
    }
}

private class SidebarFilter(
    ref: Ref<SidebarEntry>,
    createDisplay: (Player) -> PlayerSidebarDisplay,
) : SingleFilter<SidebarEntry, PlayerSidebarDisplay>(ref, createDisplay) {
    override val displays: MutableMap<UUID, PlayerSidebarDisplay>
        get() = map

    companion object {
        private val map = ConcurrentHashMap<UUID, PlayerSidebarDisplay>()
    }
}

private class PlayerSidebarDisplay(
    player: Player,
    displayKClass: KClass<out SingleFilter<SidebarEntry, *>>,
    current: Ref<SidebarEntry>,
) : PlayerSingleDisplay<SidebarEntry>(player, displayKClass, current) {
    private var lines = emptyList<Ref<LinesEntry>>()
    private var lastTitle = ""
    private var lastLines = emptyList<String>()

    override fun initialize() {
        super.initialize()
        val sidebar = ref.get() ?: return
        val title = sidebar.display(player) ?: ""

        createSidebar(title)
    }

    override fun setup() {
        super.setup()
        lines = ref.descendants(LinesEntry::class)
    }

    override fun tick() {
        super.tick()

        val sidebar = ref.get() ?: return
        val title = sidebar.display(player) ?: ""

        val lines = lines
            .filter { player.inAudience(it) }
            .sortedByDescending { it.priority }
            .mapNotNull { it.get()?.lines(player) }
            .flatMap { it.parsePlaceholders(player).lines() }

        if (lines != lastLines || title != lastTitle) {
            refreshSidebar(title, lines)
            lastTitle = title
            lastLines = lines
        }
    }

    override fun dispose() {
        super.dispose()
        disposeSidebar()
        lines = emptyList()
        lastTitle = ""
        lastLines = emptyList()
    }

    private fun createSidebar(title: String) {
        player.sendPacket(ScoreboardObjectivePacket(
            SCOREBOARD_OBJECTIVE,
            ObjectiveMode.CREATE.id,
            title.asMini(),
            ScoreboardObjectivePacket.Type.INTEGER,
            Sidebar.NumberFormat.blank(),
        ))

        player.sendPacket(DisplayScoreboardPacket(1, SCOREBOARD_OBJECTIVE))
    }

    private fun disposeSidebar() {
        player.sendPacket(ScoreboardObjectivePacket(
            SCOREBOARD_OBJECTIVE,
            ObjectiveMode.REMOVE.id,
            null,
            null,
            null,
        ))
    }

    private fun refreshSidebar(title: String, lines: List<String>) {
        player.sendPacket(ScoreboardObjectivePacket(
            SCOREBOARD_OBJECTIVE,
            ObjectiveMode.UPDATE.id,
            title.asMini(),
            ScoreboardObjectivePacket.Type.INTEGER,
            Sidebar.NumberFormat.blank(),
        ))

        player.sendPacket(DisplayScoreboardPacket(1, SCOREBOARD_OBJECTIVE))

        for ((index, line) in lines.withIndex().take(MAX_LINES)) {
            val lastLine = lastLines.getOrNull(index)
            if (lastLine == line) continue

            player.sendPacket(UpdateScorePacket(
                "${SCOREBOARD_OBJECTIVE}_line_$index",
                SCOREBOARD_OBJECTIVE,
                MAX_LINES - index,
                line.asMini(),
                Sidebar.NumberFormat.blank(),
            ))
        }

        if (lines.size < lastLines.size) {
            for (i in lines.size until lastLines.size) {
                player.sendPacket(ResetScorePacket(
                    "${SCOREBOARD_OBJECTIVE}_line_$i",
                    SCOREBOARD_OBJECTIVE,
                ))
            }
        }
    }
}
