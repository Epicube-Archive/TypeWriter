package com.typewritermc.engine.minestom.entry.roadnetwork.content

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.ok
import com.typewritermc.engine.minestom.adapt.Location
import com.typewritermc.engine.minestom.content.ContentContext
import com.typewritermc.engine.minestom.content.ContentMode
import com.typewritermc.engine.minestom.content.components.bossBar
import com.typewritermc.engine.minestom.content.components.exit
import com.typewritermc.engine.minestom.content.components.nodes
import com.typewritermc.engine.minestom.entry.entries.*
import com.typewritermc.engine.minestom.entry.forceTriggerFor
import com.typewritermc.engine.minestom.entry.roadnetwork.RoadNetworkEditorState
import com.typewritermc.engine.minestom.entry.triggerFor
import com.typewritermc.engine.minestom.utils.Color
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class SelectedNegativeNodeContentMode(
    context: ContentContext,
    player: Player,
    private val ref: Ref<RoadNetworkEntry>,
    private val selectedNodeId: RoadNodeId,
    private val initiallyScrolling: Boolean,
) : ContentMode(context, player) {
    private lateinit var editorComponent: RoadNetworkEditorComponent

    private val network get() = editorComponent.network
    private val selectedNode get() = network.negativeNodes.find { it.id == selectedNodeId }

    private var cycle = 0

    override suspend fun setup(): Result<Unit> {
        editorComponent = +RoadNetworkEditorComponent(ref)

        bossBar {
            val suffix = editorComponent.state.message

            title = "Editing <gray>${selectedNode?.id}</gray> node$suffix"
            color = when {
                editorComponent.state == RoadNetworkEditorState.Dirty -> BossBar.Color.RED
                else -> BossBar.Color.GREEN
            }
        }
        exit(doubleShiftExits = true)

        +NodeRadiusComponent(::selectedNode, initiallyScrolling, slot = 4, color = Color.BLACK) {
            editorComponent.updateAsync { roadNetwork ->
                roadNetwork.copy(negativeNodes = roadNetwork.negativeNodes.map { node ->
                    if (node.id == selectedNodeId) node.copy(
                        radius = (node.radius + it).coerceAtLeast(
                            0.5
                        )
                    ) else node
                })
            }
        }

        +RemoveNodeComponent {
            editorComponent.updateAsync { roadNetwork ->
                roadNetwork.copy(
                    negativeNodes = roadNetwork.negativeNodes.filter { it.id != selectedNodeId }
                )
            }
        }

        nodes({ network.negativeNodes }, ::showingLocation) {
            item = ItemStack.of(Material.NETHERITE_BLOCK)
            glow = if (it.id == selectedNodeId) NamedTextColor.BLACK else null
            scale = Vec(0.5, 0.5, 0.5)
            onInteract {
                if (it.id == selectedNodeId) {
                    SystemTrigger.CONTENT_POP triggerFor player
                    return@onInteract
                }
                ContentModeSwapTrigger(
                    context,
                    SelectedNegativeNodeContentMode(context, player, ref, it.id, false),
                ) triggerFor player
            }
        }
        +NegativeNodePulseComponent { network.negativeNodes.filter { it.id != selectedNodeId } }

        return ok(Unit)
    }

    override suspend fun tick() {
        super.tick()
        if (selectedNode == null) {
            // If the node is no longer in the network, we want to pop the content
            SystemTrigger.CONTENT_POP forceTriggerFor  player
        }

        cycle++
    }

    private fun showingLocation(node: RoadNode): Location = node.location.withYaw((cycle % 360).toFloat())
}