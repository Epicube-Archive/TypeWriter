package com.typewritermc.engine.paper.snippets

import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.get
import com.typewritermc.engine.paper.utils.reloadable
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import kotlin.reflect.KClass


interface SnippetDatabase {
    fun <T : Any> get(path: String, klass: KClass<T>, default: T, comment: String = ""): T
    fun <T : Any> getSnippet(path: String, klass: KClass<T>, default: T, comment: String = ""): T
    fun <T : Any> registerSnippet(path: String, klass: KClass<T>, defaultValue: T, comment: String = "")
}

class SnippetDatabaseImpl : SnippetDatabase, KoinComponent {
    private val file by lazy {
        val file = plugin.dataFolder["snippets.yml"]
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        file
    }

    private val configLoader = GsonConfigurationLoader.builder().file(file).build()
    private val configuration by reloadable { configLoader.load() }
    private val cache by reloadable { mutableMapOf<String, Any>() }

    override fun <T : Any> get(path: String, klass: KClass<T>, default: T, comment: String): T {
        val cached = cache[path]
        if (cached != null) return cached as T

        val paths = path.split(".").toMutableList()
        val node = configuration.node(paths)

        if (node.virtual()) {
            node.set(default)
            if(comment.isNotBlank()) {
                configuration.node(paths.apply { add("__comment__") }).set(comment)
            }
            configLoader.save(configuration)
            return default
        }

        val value = node.get(klass.java)
        cache[path] = value as Any
        return value
    }

    override fun <T : Any> getSnippet(path: String, klass: KClass<T>, default: T, comment: String): T {
        return get(path, klass, default, comment)
    }

    override fun <T : Any> registerSnippet(path: String, klass: KClass<T>, defaultValue: T, comment: String) {
        get(path, klass, defaultValue, comment)
    }
}