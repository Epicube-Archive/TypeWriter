package com.typewritermc.engine.paper.adapt.event

import com.github.shynixn.mccoroutine.minestom.addSuspendingListener
import lirand.api.extensions.server.server
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object EventListenerScanner {
    private val LOGGER = LoggerFactory.getLogger(EventListenerScanner::class.java)
    private val LISTENER_CACHE = ConcurrentHashMap<KClass<*>, Map<KClass<out Event>, List<EventHandlerWrapper>>>()
    private val LOOKUP = MethodHandles.lookup()

    private data class EventHandlerWrapper(val methodHandle: MethodHandle, val priority: Int) : Comparable<EventHandlerWrapper> {
        override fun compareTo(other: EventHandlerWrapper) = other.priority.compareTo(priority)
    }

    fun <T : Event> registerListeners(eventNode: EventNode<T>, listener: Any, suspending: Boolean = false) {
        val listenerClass = listener::class
        val classHandlers = LISTENER_CACHE.computeIfAbsent(listenerClass) { scanForHandlers(it.java) }

        for ((eventType, handlers) in classHandlers) {
            @Suppress("UNCHECKED_CAST")
            val eventClass = eventType.java as Class<out T>

            if (suspending) {
                registerSuspendingListener(eventNode, eventClass, listener, handlers)
            } else {
                registerSyncListener(eventNode, eventClass, listener, handlers)
            }

            LOGGER.info("Registered ${handlers.size} ${if (suspending) "suspending" else "sync"} event handler(s) for ${eventType.simpleName}")
        }
    }

    private fun scanForHandlers(listenerClass: Class<*>): Map<KClass<out Event>, List<EventHandlerWrapper>> {
        val handlers = mutableMapOf<KClass<out Event>, MutableList<EventHandlerWrapper>>()

        for (method in listenerClass.declaredMethods) {
            val annotation = method.getAnnotation(EventHandler::class.java) ?: continue
            val parameterTypes = method.parameterTypes
            if (parameterTypes.isEmpty() || !Event::class.java.isAssignableFrom(parameterTypes[0])) continue

            @Suppress("UNCHECKED_CAST")
            val eventType = parameterTypes[0].kotlin as KClass<out Event>
            try {
                val methodHandle = LOOKUP.unreflect(method)
                val wrapper = EventHandlerWrapper(methodHandle, annotation.priority)
                handlers.getOrPut(eventType) { mutableListOf() }.add(wrapper)
            } catch (e: IllegalAccessException) {
                LOGGER.error("Failed to create MethodHandle for ${method.name}", e)
            }
        }

        return handlers.mapValues { (_, v) -> v.sorted() }
    }

    private fun <T : Event> registerSyncListener(
        eventNode: EventNode<T>,
        eventClass: Class<out T>,
        listener: Any,
        handlers: List<EventHandlerWrapper>
    ) {
        eventNode.addListener(eventClass) { event ->
            for (handler in handlers) {
                try {
                    handler.methodHandle.invoke(listener, event)
                } catch (e: Throwable) {
                    LOGGER.error("Error invoking event handler for ${eventClass.simpleName}", e)
                }
            }
        }
    }

    private fun <T : Event> registerSuspendingListener(
        eventNode: EventNode<T>,
        eventClass: Class<out T>,
        listener: Any,
        handlers: List<EventHandlerWrapper>
    ) {
        eventNode.addSuspendingListener(server.minecraftServer!!, eventClass) { event ->
            for (handler in handlers) {
                try {
                    handler.methodHandle.invoke(listener, event)
                } catch (e: Throwable) {
                    LOGGER.error("Error invoking event handler for ${eventClass.simpleName}", e)
                }
            }
        }
    }
}