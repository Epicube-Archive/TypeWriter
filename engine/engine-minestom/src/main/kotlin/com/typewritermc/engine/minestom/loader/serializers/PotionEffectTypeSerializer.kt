package com.typewritermc.engine.minestom.loader.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.typewritermc.core.serialization.DataSerializer
import net.minestom.server.potion.PotionEffect
import java.lang.reflect.Type

class PotionEffectTypeSerializer : DataSerializer<PotionEffect> {
    override val type: Type = PotionEffect::class.java

    override fun serialize(src: PotionEffect?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.name() ?: "speed")
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PotionEffect {
        return PotionEffect.fromNamespaceId(json?.asString ?: "speed") ?: PotionEffect.SPEED
    }
}