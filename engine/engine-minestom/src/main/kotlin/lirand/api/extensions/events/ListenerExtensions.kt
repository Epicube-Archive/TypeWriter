package lirand.api.extensions.events

import com.typewritermc.engine.paper.adapt.Plugin
import com.typewritermc.engine.paper.adapt.event.Listener
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import kotlin.reflect.KClass

data class ListenerWithPlugin(val listener: Listener, val plugin: Plugin)

class SimpleListener : Listener

fun Listener.unregister() { /* no-op */ }

inline fun <reified T : Event> Listener.listen(
	noinline block: (event: T) -> Unit
): Unit = listen(T::class, block)

inline fun <reified T : Event> ListenerWithPlugin.listen(
	priority: Int = 0,
	noinline block: (event: T) -> Unit
): Unit = listen(T::class, block)


fun <T : Event> Listener.listen(
	type: KClass<T>,
	block: (event: T) -> Unit
) {
	MinecraftServer.getGlobalEventHandler().addListener(type.java, block)
}

fun <T : Event> ListenerWithPlugin.listen(
	type: KClass<T>,
	block: (event: T) -> Unit
): Unit = listener.listen(type, block)



inline fun <reified T : Event> Plugin.listen(
	listener: Listener = SimpleListener(),
	noinline block: (event: T) -> Unit,
): Listener = listener.apply {
	listen(block)
}


inline fun Plugin.events(
	listener: Listener = SimpleListener(),
	crossinline block: ListenerWithPlugin.() -> Unit
) = listener.apply {
	ListenerWithPlugin(listener, this@events).apply(block)
}