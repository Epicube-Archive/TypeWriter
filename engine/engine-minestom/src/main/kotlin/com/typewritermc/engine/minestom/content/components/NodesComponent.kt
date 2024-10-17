package com.typewritermc.engine.minestom.content.components

import com.typewritermc.engine.minestom.adapt.Location
import com.typewritermc.engine.minestom.adapt.event.EventHandler
import com.typewritermc.engine.minestom.adapt.event.Listener
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents
import com.typewritermc.engine.minestom.content.ComponentContainer
import com.typewritermc.engine.minestom.content.ContentComponent
import com.typewritermc.engine.minestom.events.AsyncFakeEntityInteract
import com.typewritermc.engine.minestom.extensions.packetevents.toPacketItem
import com.typewritermc.engine.minestom.extensions.packetevents.toPacketLocation
import com.typewritermc.engine.minestom.plugin
import com.typewritermc.engine.minestom.utils.distanceSqrt
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.Player.Hand
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.entity.metadata.other.InteractionMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket.InteractAt
import kotlin.math.max

const val NODE_SHOW_DISTANCE_SQUARED = 50 * 50

fun <N> ComponentContainer.nodes(
    nodeFetcher: () -> Collection<N>,
    nodeLocation: (N) -> Location,
    builder: NodeDisplayBuilder.(N) -> Unit
) = +NodesComponent(nodeFetcher, nodeLocation, builder)

class NodesComponent<N>(
    private val nodeFetcher: () -> Collection<N>,
    private val nodeLocation: (N) -> Location,
    private val builder: NodeDisplayBuilder.(N) -> Unit
) : ContentComponent, com.typewritermc.engine.minestom.adapt.event.Listener {
    private val nodes = mutableMapOf<N, NodeDisplay>()
    private var lastRefresh = 0

    private fun refreshNodes(player: Player) {
        val newNodes = nodeFetcher()
            .filter {
                (nodeLocation(it).distanceSqrt(player.position) ?: Double.MAX_VALUE) < NODE_SHOW_DISTANCE_SQUARED
            }
            .toSet()

        val toRemove = nodes.keys - newNodes
        val toRefresh = nodes.keys.intersect(newNodes)
        val toAdd = newNodes - nodes.keys
        toRemove.forEach { nodes.remove(it)?.dispose() }
        toAdd.forEach { n ->
            nodes[n] = NodeDisplayBuilder()
                .apply { builder(n) }
                .run {
                    val display = NodeDisplay()
                    display.apply(this, nodeLocation(n))
                    display
                }
                .also { it.show(player, nodeLocation(n)) }
        }
        toRefresh.forEach { n -> nodes[n]?.apply(NodeDisplayBuilder().apply { builder(n) }, nodeLocation(n)) }
        lastRefresh = 0
    }

    override suspend fun initialize(player: Player) {
        plugin.registerEvents(this)
        refreshNodes(player)
    }

    override suspend fun tick(player: Player) {
        if (lastRefresh++ > 20) {
            refreshNodes(player)
        }
    }

    @com.typewritermc.engine.minestom.adapt.event.EventHandler
    private fun onFakeEntityInteract(event: AsyncFakeEntityInteract) {
        if (event.hand != Hand.MAIN || event.action is InteractAt) return
        val entityId = event.entityId
        nodes.values.firstOrNull { it.entityId == entityId }?.interact()
    }

    override suspend fun dispose(player: Player) {
        unregister()
        nodes.values.forEach { it.dispose() }
        nodes.clear()
    }
}

class NodeDisplayBuilder {
    var item: ItemStack = ItemStack.of(Material.STONE)
    var glow: TextColor? = null
    var interaction: () -> Unit = {}
    var scale: Vec = Vec(1.0, 1.0, 1.0)

    fun onInteract(action: () -> Unit) {
        interaction = action
    }
}

private class NodeDisplay {
    private val itemDisplay = Entity(EntityType.ITEM_DISPLAY)
    private val interaction = Entity(EntityType.INTERACTION)
    private var onInteract: () -> Unit = {}
    val entityId: Int
        get() = interaction.entityId

    fun apply(builder: NodeDisplayBuilder, location: Location) {
        itemDisplay.editEntityMeta(ItemDisplayMeta::class.java) {
            it.itemStack = builder.item.toPacketItem()
            it.isHasGlowingEffect = builder.glow != null
            it.glowColorOverride = builder.glow?.value() ?: -1
            it.scale = builder.scale
            it.posRotInterpolationDuration = 30
        }

        interaction.editEntityMeta(InteractionMeta::class.java) {
            it.width = max(builder.scale.x, builder.scale.z).toFloat()
            it.height = builder.scale.y.toFloat()
        }

        onInteract = builder.interaction
        if (itemDisplay.isActive) {
            itemDisplay.teleport(location.position)
        }
        if (interaction.isActive &&
            (interaction.position.x != location.x
                    || interaction.position.y != (location.y - builder.scale.y / 2)
                    || interaction.position.z != location.z)
        ) {
            interaction.teleport(location.withY(location.y - builder.scale.y / 2).position)
        }
    }

    fun show(player: Player, location: Location) {
        itemDisplay.addViewer(player)
        itemDisplay.setInstance(location.instance!!, location.position)
        interaction.addViewer(player)
        interaction.setInstance(location.instance, location.position)
    }

    fun interact() {
        onInteract()
    }

    fun dispose() {
        itemDisplay.remove()
        interaction.remove()
    }
}