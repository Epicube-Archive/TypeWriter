package com.typewritermc.engine.paper.utils

import com.google.gson.*
import com.typewritermc.engine.paper.adapt.Location
import com.typewritermc.engine.paper.adapt.deserializeItemFromBytes
import com.typewritermc.engine.paper.adapt.serializeAsBytes
import com.typewritermc.engine.paper.logger
import net.minestom.server.MinecraftServer
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.lang.reflect.Type
import java.util.*


fun createBukkitDataParser(): Gson = GsonBuilder()
    .registerTypeAdapter(Location::class.java, LocationSerializer())
    .registerTypeHierarchyAdapter(ItemStack::class.java, ItemStackSerializer())
    .create()


class ItemStackSerializer : JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
    @Throws(JsonParseException::class)
    override fun deserialize(jsonElement: JsonElement, type: Type?, context: JsonDeserializationContext?): ItemStack {
        val data = jsonElement.asString
        if (data.isEmpty()) return ItemStack.of(Material.AIR, 0)
        return deserializeItemFromBytes(Base64.getDecoder().decode(data))
    }

    override fun serialize(src: ItemStack, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        if (src.material() == Material.AIR) return JsonPrimitive("")
        return JsonPrimitive(Base64.getEncoder().encodeToString(src.serializeAsBytes()))
    }
}

class LocationSerializer : JsonSerializer<Location>, JsonDeserializer<Location> {
    override fun serialize(src: Location, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive("${src.world?.uniqueId.toString()},${src.x},${src.y},${src.z},${src.yaw},${src.pitch}")
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Location {
        val split = json.asString.split(",")
        return Location(
            split[0].let { worldName ->
                val world = MinecraftServer.getInstanceManager().getInstance(UUID.fromString(worldName))
                if (world == null) {
                    logger.severe("World $worldName not found!")
                }
                world
            },
            split[1].toDouble(),
            split[2].toDouble(),
            split[3].toDouble(),
            split[4].toFloat(),
            split[5].toFloat()
        )
    }
}