package lirand.api.extensions.events

import com.typewritermc.engine.minestom.adapt.Plugin
import com.typewritermc.engine.minestom.adapt.event.Listener
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import kotlin.reflect.KClass

data class ListenerWithPlugin(val listener: com.typewritermc.engine.minestom.adapt.event.Listener, val plugin: Plugin)

class SimpleListener : com.typewritermc.engine.minestom.adapt.event.Listener

fun com.typewritermc.engine.minestom.adapt.event.Listener.unregister() { /* no-op */ }

inline fun <reified T : Event> com.typewritermc.engine.minestom.adapt.event.Listener.listen(
	noinline block: (event: T) -> Unit
): Unit = listen(T::class, block)

inline fun <reified T : Event> ListenerWithPlugin.listen(
	priority: Int = 0,
	noinline block: (event: T) -> Unit
): Unit = listen(T::class, block)


fun <T : Event> com.typewritermc.engine.minestom.adapt.event.Listener.listen(
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
	listener: com.typewritermc.engine.minestom.adapt.event.Listener = SimpleListener(),
	noinline block: (event: T) -> Unit,
): com.typewritermc.engine.minestom.adapt.event.Listener = listener.apply {
	listen(block)
}


inline fun Plugin.events(
	listener: com.typewritermc.engine.minestom.adapt.event.Listener = SimpleListener(),
	crossinline block: ListenerWithPlugin.() -> Unit
) = listener.apply {
	ListenerWithPlugin(listener, this@events).apply(block)
}