package com.typewritermc.basic.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.minestom.entry.entries.AudienceDisplay
import com.typewritermc.engine.minestom.entry.entries.AudienceEntry
import com.typewritermc.engine.minestom.entry.entries.GroupEntry
import com.typewritermc.engine.minestom.entry.entries.RoadNetworkEntry
import com.typewritermc.engine.minestom.entry.roadnetwork.gps.MultiPathStreamDisplay

@Entry(
    "group_members_path_stream",
    "A Path Stream to Group Members",
    Colors.GREEN,
    "material-symbols:conversion-path"
)
/**
 * The `Group Members Path Stream` entry is a path stream that shows the path to each group member.
 * The 'Group Members' are determined by the group members that are in the same group as the player.
 *
 * When the group is not set, the path stream will not display anything.
 *
 * ## How could this be used?
 * This could be used to show a path to each group member in a group of players.
 * When a player wants to find any other group member, they can follow the respective path.
 */
class GroupMembersPathStream(
    override val id: String = "",
    override val name: String = "",
    val road: Ref<RoadNetworkEntry> = emptyRef(),
    val group: Ref<out GroupEntry> = emptyRef(),
) : AudienceEntry {
    override fun display(): AudienceDisplay = MultiPathStreamDisplay(road, endLocations = { player ->
        group.get()?.group(player)?.players
            ?.filter { it != player }
            ?.map { it.location }
            ?: emptyList()
    })
}