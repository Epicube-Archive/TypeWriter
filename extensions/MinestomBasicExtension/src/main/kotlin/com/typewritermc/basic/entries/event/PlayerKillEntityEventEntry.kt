package com.typewritermc.basic.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.engine.minestom.entry.*
import com.typewritermc.engine.minestom.entry.entries.EventEntry
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.event.entity.EntityDeathEvent
import java.util.*

@Entry("on_player_kill_entity", "When a player kills an entity", Colors.YELLOW, "fa6-solid:skull")
/**
 * The `Player Kill Entity Event` is fired when a player kills an entity. If you want to detect when a player kills another player, use the [`Player Kill Player Event`](on_player_kill_player) instead.
 *
 * ## How could this be used?
 *
 * This event could be used to detect when a player kills a boss, and give them some rewards. It could also be used to create a custom mob that drops items when killed by a player.
 */
class PlayerKillEntityEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val entityType: Optional<EntityType> = Optional.empty(),
) : EventEntry


@EntryListener(PlayerKillEntityEventEntry::class)
fun onKill(event: EntityDeathEvent, query: Query<PlayerKillEntityEventEntry>) {
    val killer = (event.entity as LivingEntity).lastDamageSource?.attacker ?: return
    if(killer !is Player) {
        return
    }

    query findWhere { entry ->
        entry.entityType.map { it == event.entity.entityType }.orElse(true)
    } triggerAllFor killer
}