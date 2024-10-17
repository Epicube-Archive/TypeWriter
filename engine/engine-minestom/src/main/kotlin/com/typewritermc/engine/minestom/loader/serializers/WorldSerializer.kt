package com.typewritermc.engine.minestom.loader.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.typewritermc.core.utils.point.World
import com.typewritermc.core.serialization.DataSerializer
import net.minestom.server.MinecraftServer
import java.lang.reflect.Type
import java.util.*

class WorldSerializer : DataSerializer<World> {
    override val type: Type = World::class.java

    override fun serialize(src: World?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.identifier ?: "")
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): World {
        val world = json?.asString ?: ""

        val instanceManager = MinecraftServer.getInstanceManager();
        val bukkitWorld = instanceManager.getInstance(UUID.fromString(world))
            ?: instanceManager.instances.firstOrNull { it.uniqueId.equals(world) }
            ?: instanceManager.instances.firstOrNull()
            ?: throw IllegalArgumentException("Could not find instance '$world' for location, and no default instance available.")

        return World(bukkitWorld.uniqueId.toString())
    }
}