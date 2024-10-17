package com.typewritermc.engine.minestom.entry.roadnetwork.content

import com.extollit.gaming.ai.path.model.IPath
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.loopingDistance
import com.typewritermc.core.utils.ok
import com.typewritermc.engine.minestom.adapt.Location
import com.typewritermc.engine.minestom.adapt.event.EventHandler
import com.typewritermc.engine.minestom.adapt.event.Listener
import com.typewritermc.engine.minestom.content.ContentComponent
import com.typewritermc.engine.minestom.content.ContentContext
import com.typewritermc.engine.minestom.content.ContentMode
import com.typewritermc.engine.minestom.content.components.*
import com.typewritermc.engine.minestom.entry.entries.*
import com.typewritermc.engine.minestom.entry.forceTriggerFor
import com.typewritermc.engine.minestom.entry.roadnetwork.RoadNetworkEditorState
import com.typewritermc.engine.minestom.entry.roadnetwork.gps.roadNetworkFindPath
import com.typewritermc.engine.minestom.entry.roadnetwork.pathfinding.PFInstanceSpace
import com.typewritermc.engine.minestom.entry.triggerFor
import com.typewritermc.engine.minestom.plugin
import com.typewritermc.engine.minestom.utils.*
import com.typewritermc.engine.minestom.utils.ThreadType.DISPATCHERS_ASYNC
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import org.koin.core.component.KoinComponent
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class SelectedRoadNodeContentMode(
    context: ContentContext,
    player: Player,
    private val ref: Ref<RoadNetworkEntry>,
    private val selectedNodeId: RoadNodeId,
    private val initiallyScrolling: Boolean,
) : ContentMode(context, player), KoinComponent {
    private lateinit var editorComponent: RoadNetworkEditorComponent

    private val network get() = editorComponent.network
    private val selectedNode get() = network.nodes.find { it.id == selectedNodeId }

    private var cycle = 0

    override suspend fun setup(): Result<Unit> {
        editorComponent = +RoadNetworkEditorComponent(ref)

        val pathsComponent = +SelectedNodePathsComponent(::selectedNode, ::network)
        bossBar {
            var suffix = editorComponent.state.message
            if (!pathsComponent.isPathsLoaded) suffix += " <gray><i>(calculating edges)</i></gray>"

            title = "Editing <gray>${selectedNode?.id}</gray> node$suffix"
            color = when {
                editorComponent.state == RoadNetworkEditorState.Dirty -> BossBar.Color.RED
                !pathsComponent.isPathsLoaded -> BossBar.Color.PURPLE
                else -> BossBar.Color.GREEN
            }
        }
        exit(doubleShiftExits = true)

        +NodeRadiusComponent(::selectedNode, initiallyScrolling) { radiusChange ->
            editorComponent.updateAsync { roadNetwork ->
                roadNetwork.copy(nodes = roadNetwork.nodes.map { node ->
                    if (node.id == selectedNodeId) node.copy(
                        radius = (node.radius + radiusChange).coerceAtLeast(
                            0.5
                        )
                    ) else node
                })
            }
        }

        +RemoveNodeComponent {
            editorComponent.updateAsync { roadNetwork ->
                roadNetwork.copy(
                    nodes = roadNetwork.nodes.filter { it.id != selectedNodeId },
                    edges = roadNetwork.edges.filter { it.start != selectedNodeId && it.end != selectedNodeId },
                    modifications = roadNetwork.modifications.filter {
                        if (it !is RoadModification.EdgeModification) return@filter true
                        it.start != selectedNodeId && it.end != selectedNodeId
                    }
                )
            }
        }

        +ModificationComponent(::selectedNode, ::network)

        nodes({ network.nodes }, ::showingLocation) { node ->
            item = ItemStack.of(node.material(network.modifications))
            glow = when {
                node == selectedNode -> NamedTextColor.WHITE
                network.edges.any { it.start == selectedNodeId && it.end == node.id } -> NamedTextColor.BLUE
                network.modifications.containsRemoval(
                    selectedNodeId,
                    node.id
                ) && network.modifications.containsRemoval(node.id, selectedNodeId) -> NamedTextColor.RED

                network.modifications.containsRemoval(selectedNodeId, node.id) -> NamedTextColor.GOLD
                network.modifications.containsAddition(
                    selectedNodeId,
                    node.id
                ) && network.modifications.containsAddition(node.id, selectedNodeId) -> NamedTextColor.GREEN

                network.modifications.containsAddition(selectedNodeId, node.id) -> TextColor.color(0x4fec97)
                else -> null
            }
            scale = Vec(0.5, 0.5, 0.5)
            onInteract { interactWithNode(node) }
        }

        nodes({ network.negativeNodes }, ::showingLocation) {
            item = ItemStack.of(Material.NETHERITE_BLOCK)
            glow = NamedTextColor.BLACK
            scale = Vec(0.5, 0.5, 0.5)
            onInteract {
                ContentModeSwapTrigger(
                    context,
                    SelectedNegativeNodeContentMode(
                        context,
                        player,
                        ref,
                        it.id,
                        false
                    )
                ) triggerFor player
            }
        }
        +NegativeNodePulseComponent { network.negativeNodes }

        return ok(Unit)
    }

    private fun showingLocation(node: RoadNode): Location = node.location.withYaw((cycle % 360).toFloat())

    private fun interactWithNode(node: RoadNode) {
        if (node == selectedNode) {
            SystemTrigger.CONTENT_POP triggerFor player
            return
        }

        if (player.heldSlot.toInt() == 5) {
            edgeAddition(node)
            return
        }

        if (player.heldSlot.toInt() == 6) {
            edgeRemoval(node)
            return
        }

        if (player.itemInMainHand.isAir) {
            ContentModeSwapTrigger(
                context,
                SelectedRoadNodeContentMode(context, player, ref, node.id, false),
            ) triggerFor player
            return
        }
    }

    /**
     * Toggle the edge between modified and unmodified bidirectional
     * When the player is shifting, then we want to do it only directionally
     */
    private inline fun <reified M : RoadModification.EdgeModification> edgeModification(
        node: RoadNode,
        create: (RoadNodeId, RoadNodeId) -> M,
        crossinline modifyNetwork: (RoadNode, RoadNode, RoadNetwork) -> RoadNetwork,
    ) {
        if (node == selectedNode) return
        // If it contains the other modification, we don't want to do anything
        val containsOtherModification =
            network.modifications.any {
                it is RoadModification.EdgeModification && it !is M
                        && it.start == selectedNodeId && it.end == node.id
            }
        if (containsOtherModification) return

        player.playSound("ui.button.click")

        val containsModification =
            network.modifications.any { it is M && it.start == selectedNodeId && it.end == node.id }

        val modification = create(selectedNodeId, node.id)
        val reverseModification = create(node.id, selectedNodeId)

        if (containsModification) {
            editorComponent.updateAsync { roadNetwork ->
                roadNetwork.copy(
                    modifications = roadNetwork.modifications.filter {
                        it != modification && if (player.isSneaking) it != reverseModification else true
                    }
                )
            }
        } else {
            val selectedNode = selectedNode ?: return
            editorComponent.updateAsync { roadNetwork ->
                val modifications = if (player.isSneaking) {
                    roadNetwork.modifications + modification
                } else {
                    roadNetwork.modifications + modification + reverseModification
                }

                val n1 = roadNetwork.copy(
                    modifications = modifications
                )
                if (player.isSneaking) {
                    modifyNetwork(selectedNode, node, n1)
                } else {
                    modifyNetwork(selectedNode, node, modifyNetwork(node, selectedNode, n1))
                }
            }
        }
    }

    private fun edgeAddition(node: RoadNode) {
        edgeModification(
            node,
            { start, end -> RoadModification.EdgeAddition(start, end, 0.0) }) { start, end, network ->
            network.copy(
                edges = network.edges + RoadEdge(start.id, end.id, 0.0)
            )
        }
    }

    private fun edgeRemoval(node: RoadNode) {
        edgeModification(node, { start, end -> RoadModification.EdgeRemoval(start, end) }) { start, end, network ->
            network.copy(
                edges = network.edges.filter { it.start != start.id || it.end != end.id }
            )
        }
    }

    override suspend fun tick() {
        cycle++

        if (selectedNode == null) {
            // If the node is no longer in the network, we want to pop the content
            SystemTrigger.CONTENT_POP forceTriggerFor player
        }

        super.tick()
    }
}

class RemoveNodeComponent(
    private val slot: Int = 0,
    private val onRemove: () -> Unit,
) : ItemComponent {
    override fun item(player: Player): Pair<Int, IntractableItem> {
        return slot to (ItemStack.of(Material.REDSTONE_BLOCK)
            .withCustomName("<red><b>Remove Node".asMini())
            .withLore("<line> <gray>Careful! This action is irreversible.".asMini())
            .onInteract {
                onRemove()
            })
    }
}

private class SelectedNodePathsComponent(
    private val nodeFetcher: () -> RoadNode?,
    private val networkFetcher: () -> RoadNetwork,
) : ContentComponent {
    private var paths: Map<RoadEdge, IPath>? = null
    val isPathsLoaded: Boolean
        get() = paths != null

    override suspend fun initialize(player: Player) {
        DISPATCHERS_ASYNC.launch {
            paths = loadEdgePaths()
        }
    }

    private fun loadEdgePaths(): Map<RoadEdge, IPath> {
        val node = nodeFetcher() ?: return emptyMap()
        val network = networkFetcher()
        val nodes = network.nodes.associateBy { it.id }
        val instance = PFInstanceSpace(node.location.world!!)
        return network.edges.filter { it.start == node.id }
            .mapNotNull { edge ->
                val start = nodes[edge.start] ?: return@mapNotNull null
                val end = nodes[edge.end] ?: return@mapNotNull null

                val path = roadNetworkFindPath(
                    start,
                    end,
                    instance = instance,
                    nodes = network.nodes,
                    negativeNodes = network.negativeNodes
                ) ?: return@mapNotNull null
                edge to path
            }
            .toMap()
    }

    private fun refreshEdges() {
        val node = nodeFetcher() ?: return
        val network = networkFetcher()
        val edges = network.edges.filter { it.start == node.id }
        if (paths?.keys?.toSet() == edges.toSet()) return
        paths = loadEdgePaths()
    }

    private var tick = 0
    override suspend fun tick(player: Player) {
        if (paths == null) return
        if (tick++ % 20 == 0) {
            refreshEdges()
        }
        if (tick++ % 3 != 0) return

        paths?.forEach { (edge, path) ->
            path.forEach {
                player.sendPacket(ParticlePacket(
                    Particle.DUST.withProperties(NetworkEdgesComponent.colorFromHash(edge.end.hashCode()).toPacketColor(), 1f),
                    true,
                    Vec(it.coordinates().x + 0.5, it.coordinates().y + 0.5, it.coordinates().z + 0.5),
                    Vec.ZERO,
                    0f,
                    1
                ))
            }
        }
    }

    override suspend fun dispose(player: Player) {}
}

class NodeRadiusComponent(
    private val nodeFetcher: () -> RoadNode?,
    private val initiallyScrolling: Boolean,
    private val slot: Int = 2,
    private val color: Color = Color.RED,
    private val editRadius: (Double) -> Unit,
) : ItemComponent, com.typewritermc.engine.minestom.adapt.event.Listener {

    private var scrolling: UUID? = null

    override fun item(player: Player): Pair<Int, IntractableItem> {
        val item = if (scrolling != null) {
            ItemStack.of(Material.CALIBRATED_SCULK_SENSOR)
                .withCustomName("<yellow><b>Selecting Radius".asMini())
                .withLore("<line> <gray>Right click to set the radius of the node.".asMini())
                .unClickable()
        } else {
            ItemStack.of(Material.SCULK_SENSOR)
                .withCustomName("<yellow><b>Change Radius".asMini())
                .withLore("<line> <gray>Current radius: <white>${nodeFetcher()?.radius}".asMini())
                .unClickable()
        }
        return slot to (item onInteract {
            scrolling = if (scrolling == player.uuid) {
                null
            } else {
                player.uuid
            }
            player.playSound("ui.button.click")
        })
    }

    override suspend fun initialize(player: Player) {
        super.initialize(player)
        if (initiallyScrolling) {
            // When we start out already selecting, we want to make sure the player is holding the correct item
            // So that they can stop changing the radius
            player.setHeldItemSlot(slot.toByte())
            scrolling = player.uuid
        }
        plugin.registerEvents(this)
    }

    @com.typewritermc.engine.minestom.adapt.event.EventHandler
    private fun onScroll(event: PlayerChangeHeldSlotEvent) {
        val player = event.player
        val previousSlot = player.heldSlot
        if (player.uuid != scrolling) return
        val delta = loopingDistance(previousSlot.toInt(), event.slot.toInt(), 8)
        val radiusMultiplier = if (player.isSneaking) 0.1 else 0.5
        editRadius(delta * radiusMultiplier)
        val sound = if (player.isSneaking) {
            "block.note_block.bell"
        } else {
            "block.note_block.hat"
        }
        player.playSound(sound, pitch = 1f + (delta * 0.1f), volume = 0.5f)
        event.isCancelled = true
    }

    private var tick: Int = 0
    override suspend fun tick(player: Player) {
        super.tick(player)
        if (tick++ % 2 == 0) return
        val node = nodeFetcher() ?: return
        val radius = node.radius
        val location = node.location

        location.particleSphere(player, radius, color, phiDivisions = 16, thetaDivisions = 8)
    }

    override suspend fun dispose(player: Player) {
        super.dispose(player)
        unregister()
    }
}

private class ModificationComponent(
    private val nodeFetcher: () -> RoadNode?,
    private val networkFetcher: () -> RoadNetwork,
) : ContentComponent, ItemsComponent {
    override fun items(player: Player): Map<Int, IntractableItem> {
        val map = mutableMapOf<Int, IntractableItem>()
        val node = nodeFetcher() ?: return map
        val network = networkFetcher()

        map[5] = ItemStack.of(Material.EMERALD)
            .withCustomName("<green><b>Add Fast Travel Connection".asMini())
            .withLore("""
                    |<line> <gray>Click on a unconnected node to <green>add a fast travel connection</green> to it.
                    |<line> <gray>Click on a modified node to <red>remove the connection</red>.
                    |
                    |<line> <gray>If you only want to connect one way, hold <red>Shift</red> while clicking.
                    |""".trimMargin().asMini())
            .unClickable()
            .onInteract {}

        val hasEdges = network.edges.any { it.start == node.id }
        if (hasEdges) {
            map[6] = ItemStack.of(Material.REDSTONE)
                .withCustomName("<red><b>Remove Edge".asMini())
                .withLore("""
                    |<line> <gray>Click on a connected node to <red>force remove the edge</red> between them.
                    |<line> <gray>Click on a modified node to allow the edge to be added again.
                    |
                    |<line> <gray>If you only want to remove one way, hold <red>Shift</red> while clicking.
                """.trimMargin().asMini())
                .unClickable()
                .onInteract {}
        }

        return map
    }

    override suspend fun initialize(player: Player) {}

    override suspend fun tick(player: Player) {}

    override suspend fun dispose(player: Player) {}
}