package com.typewritermc.engine.minestom.loader.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.typewritermc.core.serialization.DataSerializer
import net.minestom.server.coordinate.Vec
import java.lang.reflect.Type

class VectorSerializer : DataSerializer<Vec> {
    override val type: Type = Vec::class.java

    override fun serialize(src: Vec?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val obj = JsonObject()
        obj.addProperty("x", src?.x ?: 0.0)
        obj.addProperty("y", src?.y ?: 0.0)
        obj.addProperty("z", src?.z ?: 0.0)
        return obj
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Vec {
        val obj = json?.asJsonObject ?: JsonObject()
        val x = obj.getAsJsonPrimitive("x")?.asDouble ?: 0.0
        val y = obj.getAsJsonPrimitive("y")?.asDouble ?: 0.0
        val z = obj.getAsJsonPrimitive("z")?.asDouble ?: 0.0
        return Vec(x, y, z)
    }
}