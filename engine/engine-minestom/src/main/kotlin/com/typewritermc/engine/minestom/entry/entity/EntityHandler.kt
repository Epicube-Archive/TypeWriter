package com.typewritermc.engine.minestom.entry.entity

import com.typewritermc.engine.minestom.adapt.event.EventHandler
import com.typewritermc.engine.minestom.adapt.event.Listener
import com.typewritermc.engine.minestom.entry.AudienceManager
import com.typewritermc.engine.minestom.events.AsyncEntityDefinitionInteract
import com.typewritermc.engine.minestom.events.AsyncFakeEntityInteract
import com.typewritermc.engine.minestom.utils.callEvent
import lirand.api.extensions.server.server
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EntityHandler : com.typewritermc.engine.minestom.adapt.event.Listener, KoinComponent {
    private val audienceManager: AudienceManager by inject()
    fun initialize() {
        server.registerEvents(this)
    }

    @com.typewritermc.engine.minestom.adapt.event.EventHandler
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