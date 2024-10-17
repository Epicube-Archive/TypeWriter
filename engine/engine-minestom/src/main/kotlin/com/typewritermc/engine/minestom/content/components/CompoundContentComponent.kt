package com.typewritermc.engine.minestom.content.components

import com.typewritermc.engine.minestom.content.ContentComponent
import com.typewritermc.engine.minestom.content.ComponentContainer
import net.minestom.server.entity.Player

abstract class CompoundContentComponent : ContentComponent, ComponentContainer {
    override val components = mutableListOf<ContentComponent>()
    override suspend fun initialize(player: Player) {
        components.forEach { it.initialize(player) }
    }

    override suspend fun tick(player: Player) {
        components.forEach { it.tick(player) }
    }

    override suspend fun dispose(player: Player) {
        components.forEach { it.dispose(player) }
        components.clear()
    }
}