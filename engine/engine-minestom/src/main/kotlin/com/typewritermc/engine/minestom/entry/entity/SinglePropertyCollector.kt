package com.typewritermc.engine.minestom.entry.entity

import com.typewritermc.engine.minestom.entry.entries.EntityProperty
import com.typewritermc.engine.minestom.entry.entries.PropertyCollector
import com.typewritermc.engine.minestom.entry.entries.PropertyCollectorSupplier
import com.typewritermc.engine.minestom.entry.entries.PropertySupplier
import net.minestom.server.entity.Player
import kotlin.reflect.KClass

class SinglePropertyCollector<P : EntityProperty>(
    private val suppliers: List<PropertySupplier<out P>>,
    override val type: KClass<P>,
    val default: P? = null,
) : PropertyCollector<P> {
    override fun collect(player: Player): P? {
        return suppliers.firstOrNull { it.canApply(player) }?.build(player) ?: default
    }
}

open class SinglePropertyCollectorSupplier<P : EntityProperty>(override val type: KClass<P>, val default: P? = null) :
    PropertyCollectorSupplier<P> {
    override fun collector(suppliers: List<PropertySupplier<out P>>): PropertyCollector<P> =
        SinglePropertyCollector(suppliers, type, default)
}