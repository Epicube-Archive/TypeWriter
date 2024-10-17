package com.typewritermc.engine.paper.entry.entity

import com.typewritermc.engine.paper.adapt.event.EventHandler
import com.typewritermc.engine.paper.adapt.event.Listener
import com.typewritermc.engine.paper.entry.AudienceManager
import com.typewritermc.engine.paper.events.AsyncEntityDefinitionInteract
import com.typewritermc.engine.paper.events.AsyncFakeEntityInteract
import com.typewritermc.engine.paper.utils.callEvent
import lirand.api.extensions.server.server
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EntityHandler : Listener, KoinComponent {
    private val audienceManager: AudienceManager by inject()
    fun initialize() {
        server.registerEvents(this)
    }

    @EventHandler
    fun onPacketReceive(event: PlayerPacketEvent) {
        val packet = event.packet
        if (packet !is ClientInteractEntityPacket) return

        val entityId = packet.targetId
        val player = event.player

        AsyncFakeEntityInteract(player, entityId, player.playerMeta.activeHand, packet.type).callEvent()

        val display = audienceManager
            .findDisplays(ActivityEntityDisplay::class)
            .firstOrNull { it.playerSeesEntity(player.uuid, entityId) } ?: return

        val definition = display.definition ?: return
        AsyncEntityDefinitionInteract(player, entityId, definition, player.playerMeta.activeHand, packet.type).callEvent()
    }

    fun shutdown() {
    }
}