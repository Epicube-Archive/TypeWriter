package com.typewritermc.engine.minestom.content.modes

import com.github.shynixn.mccoroutine.minestom.launch
import kotlinx.coroutines.delay
import com.typewritermc.engine.minestom.content.ContentContext
import com.typewritermc.engine.minestom.content.ContentMode
import com.typewritermc.engine.minestom.content.entryId
import com.typewritermc.engine.minestom.content.fieldPath
import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.minestom.entry.entries.SystemTrigger.CONTENT_END
import com.typewritermc.engine.minestom.entry.fieldValue
import com.typewritermc.engine.minestom.entry.triggerFor
import com.typewritermc.core.utils.failure
import com.typewritermc.core.utils.ok
import com.typewritermc.engine.minestom.logger
import lirand.api.extensions.server.server
import net.minestom.server.entity.Player
import java.lang.reflect.Type
import kotlin.time.Duration.Companion.milliseconds

abstract class ImmediateFieldValueContentMode<T : Any>(context: ContentContext, player: Player) :
    ContentMode(context, player) {
        abstract val type: Type

    override suspend fun setup(): Result<Unit> {
        val entryId = context.entryId
            ?: return failure("No entryId found for ${this::class.simpleName}. This is a bug. Please report it.")

        val fieldPath = context.fieldPath
            ?: return failure("No fieldPath found for ${this::class.simpleName}. This is a bug. Please report it.")

        // Needs to complete the initialisation so that we can properly get the value and end the content mode
        server.minecraftServer?.launch {
            delay(200.milliseconds)
            try {
                val value = value()
                Ref(entryId, Entry::class).fieldValue(fieldPath, value, type)
            } catch (e: Exception) {
                logger.severe("Failed to set field value for ${this::class.simpleName}, with context: $context. This is a bug. Please report it.")
                e.printStackTrace()
            } finally {
                CONTENT_END triggerFor player
            }
        }

        return ok(Unit)
    }

    abstract fun value(): T

}