package com.typewritermc.engine.paper.adapt.event;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventListenerScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerScanner.class);

    private static final Map<Class<?>, Map<Class<? extends Event>, List<EventHandlerWrapper>>> LISTENER_CACHE = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private record EventHandlerWrapper(MethodHandle methodHandle, int priority) implements Comparable<EventHandlerWrapper> {

        @Override
        public int compareTo(EventHandlerWrapper other) {
            return Integer.compare(other.priority, this.priority);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void registerListeners(EventNode<T> eventNode, Object listener) {
        var listenerClass = listener.getClass();
        var classHandlers = LISTENER_CACHE.computeIfAbsent(listenerClass, k -> new ConcurrentHashMap<>());

        if (classHandlers.isEmpty()) {
            for (var method : listenerClass.getDeclaredMethods()) {
                var annotation = method.getAnnotation(EventHandler.class);
                if (annotation != null) {
                    var parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length > 0 && Event.class.isAssignableFrom(parameterTypes[0])) {
                        var eventType = (Class<? extends Event>) parameterTypes[0];
                        try {
                            var methodHandle = LOOKUP.unreflect(method);
                            var wrapper = new EventHandlerWrapper(methodHandle, annotation.priority());
                            classHandlers.computeIfAbsent(eventType, e -> new ArrayList<>()).add(wrapper);
                        } catch (IllegalAccessException e) {
                            LOGGER.error("Failed to create MethodHandle for " + method.getName(), e);
                        }
                    }
                }
            }

            // Sort handlers by priority
            for (var handlers : classHandlers.values()) {
                Collections.sort(handlers);
            }
        }

        for (var entry : classHandlers.entrySet()) {
            var eventType = (Class<? extends T>) entry.getKey();
            var handlers = entry.getValue();

            eventNode.addListener(eventType, event -> {
                for (var handler : handlers) {
                    try {
                        handler.methodHandle.invoke(listener, event);
                    } catch (Throwable e) {
                        LOGGER.error("Error invoking event handler for " + eventType.getSimpleName(), e);
                    }
                }
            });

            LOGGER.info("Registered " + handlers.size() + " event handler(s) for " + eventType.getSimpleName());
        }
    }
}