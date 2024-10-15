package com.typewritermc.engine.paper.content

import com.typewritermc.engine.paper.content.components.IntractableItem
import com.typewritermc.engine.paper.content.components.ItemsComponent
import net.minestom.server.entity.Player

abstract class ContentMode(
    val context: ContentContext,
    val player: Player,
) : ComponentContainer {
    override val components = mutableListOf<ContentComponent>()

    /**
     * If the result is [Result.Failure], the content mode will not be started.
     */
    abstract suspend fun setup(): Result<Unit>

    open suspend fun initialize() {
        components.forEach { it.initialize(player) }
    }
    open suspend fun tick() {
        components.forEach { it.tick(player) }
    }
    open suspend fun dispose() {
        components.forEach { it.dispose(player) }
    }

    fun items(): Map<Int, IntractableItem> {
        return components.filterIsInstance<ItemsComponent>().flatMap { it.items(player).toList() }.toMap()
    }
}

interface ComponentContainer {
    val components: MutableList<ContentComponent>
    operator fun <C : ContentComponent> C.unaryPlus(): C {
        components.add(this)
        return this
    }
}