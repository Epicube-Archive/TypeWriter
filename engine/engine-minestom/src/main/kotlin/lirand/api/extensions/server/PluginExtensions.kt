package lirand.api.extensions.server

import com.typewritermc.engine.minestom.adapt.Plugin
import com.typewritermc.engine.minestom.adapt.event.EventListenerScanner
import com.typewritermc.engine.minestom.adapt.event.Listener
import net.minestom.server.MinecraftServer

fun Plugin.registerEvents(
	vararg listeners: com.typewritermc.engine.minestom.adapt.event.Listener
) = listeners.forEach {
	EventListenerScanner.registerListeners(MinecraftServer.getGlobalEventHandler(), it, false)
}

fun Plugin.registerSuspendingEvents(
	vararg listeners: com.typewritermc.engine.minestom.adapt.event.Listener
) = listeners.forEach {
	EventListenerScanner.registerListeners(MinecraftServer.getGlobalEventHandler(), it, true)
}