package lirand.api.extensions.server

import com.typewritermc.engine.paper.adapt.Server
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import org.bukkit.WorldCreator

val server get() = Server.instance
val onlinePlayers: MutableCollection<Player> get() = MinecraftServer.getConnectionManager().onlinePlayers

val craftBukkitPackage = server.javaClass.getPackage().name

val Server.mainWorld get() = worlds[0]!!

fun WorldCreator.create() = server.createWorld(this)