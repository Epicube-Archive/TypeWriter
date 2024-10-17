package com.typewritermc.engine.minestom.utils

import com.typewritermc.engine.minestom.logger
import com.typewritermc.engine.minestom.plugin
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.safeCast

inline fun <reified T : Any> config(key: String, default: T, comment: String? = null) =
    ConfigPropertyDelegate(key, T::class, default, comment)

class ConfigPropertyDelegate<T : Any>(
    private val key: String,
    private val klass: KClass<T>,
    private val default: T,
    private val comments: String?,
) {
    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T {
        val paths = key.split(".").toMutableList()
        val node = plugin.config.node(paths)
        if (node.virtual()) {
            node.set(default)
            if(comments != null) {
                plugin.config.node(paths.apply { add("__comment__") }).set(comments)
            }
            plugin.saveConfig()
            return default
        }

        val value = node.get(klass.java)
        if (value == null) {
            logger.warning("Invalid value for config key '$key', expected ${klass.simpleName}")
            return default
        }
        return value
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T = getValue(null, property)
}

inline fun <reified T : Any> optionalConfig(key: String) =
    OptionalConfigPropertyDelegate(key, T::class)

class OptionalConfigPropertyDelegate<T : Any>(
    private val key: String,
    private val klass: KClass<T>,
) {
    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T? {
        val paths = key.split(".")
        val node = plugin.config.node(paths)
        if (node.virtual()) {
            return null
        }

        val value = node.get(klass.java)
        if (value == null) {
            logger.warning("Invalid value for config key '$key', expected ${klass.simpleName}")
            return null
        }
        return value
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T? = getValue(null, property)
}