package com.typewritermc.engine.minestom.entry.entity

import com.typewritermc.engine.minestom.entry.entries.EntityProperty
import net.minestom.server.entity.Player

data class SkinProperty(
    val texture: String = "",
    val signature: String = "",
) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<SkinProperty>(SkinProperty::class)
}

val Player.skin: SkinProperty
    get() {
        if (this.skin == null) return SkinProperty()

        val textures: String = this.skin?.textures() ?: ""
        if (textures.isEmpty()) return SkinProperty()

        return SkinProperty(this.skin?.textures() ?: "", this.skin?.signature() ?: "")
    }