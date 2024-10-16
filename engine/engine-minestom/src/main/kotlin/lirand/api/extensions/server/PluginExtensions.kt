package lirand.api.extensions.server

import com.typewritermc.engine.paper.adapt.Plugin
import com.typewritermc.engine.paper.adapt.event.EventListenerScanner
import com.typewritermc.engine.paper.adapt.event.Listener
import net.minestom.server.MinecraftServer

fun Plugin.registerEvents(
	vararg listeners: Listener
) = listeners.forEach {
	EventListenerScanner.registerListeners(MinecraftServer.getGlobalEventHandler(), it)
}

fun Plugin.registerSuspendingEvents(
	vararg listeners: Listener
) = listeners.forEach {
	EventListenerScanner.registerListeners(MinecraftServer.getGlobalEventHandler(), it)
	// TODO: suspending events ?
	//server.pluginManager.registerSuspendingEvents(it, this)
}