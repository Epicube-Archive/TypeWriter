package com.typewritermc.basic.entries.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.cinematic.SimpleCinematicAction
import com.typewritermc.engine.minestom.entry.entries.CinematicAction
import com.typewritermc.engine.minestom.entry.entries.PrimaryCinematicEntry
import com.typewritermc.engine.minestom.entry.entries.Segment
import com.typewritermc.engine.minestom.utils.EffectStateProvider
import com.typewritermc.engine.minestom.utils.PlayerState
import com.typewritermc.engine.minestom.utils.ThreadType.SYNC
import com.typewritermc.engine.minestom.utils.restore
import com.typewritermc.engine.minestom.utils.state
import net.minestom.server.entity.Player
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import kotlin.experimental.or

@Entry(
    "potion_effect_cinematic",
    "Apply different potion effects to the player during a cinematic",
    Colors.CYAN,
    "fa6-solid:flask-vial"
)
/**
 * The `PotionEffectCinematicEntry` is used to apply different potion effects to the player during a cinematic.
 *
 * ## How could this be used?
 * This can be used to dynamically apply effects like blindness, slowness, etc., at different times
 * during a cinematic, enhancing the storytelling or gameplay experience.
 */
class PotionEffectCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(icon = "heroicons-solid:status-offline")
    val segments: List<PotionEffectSegment> = emptyList()
) : PrimaryCinematicEntry {
    override fun createSimulating(player: Player): CinematicAction? = null
    override fun create(player: Player): CinematicAction {
        return PotionEffectCinematicAction(
            player,
            this
        )
    }
}

data class PotionEffectSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
    val potionEffectType: PotionEffect = PotionEffect.BLINDNESS,
    @Default("1")
    val strength: Int = 1,
    val ambient: Boolean = false,
    val particles: Boolean = false,
    @Help("Whether the icon should be displayed in the top left corner of the screen.")
    val icon: Boolean = false,
) : Segment

class PotionEffectCinematicAction(
    private val player: Player,
    entry: PotionEffectCinematicEntry
) : SimpleCinematicAction<PotionEffectSegment>() {

    private var state: PlayerState? = null

    override val segments: List<PotionEffectSegment> = entry.segments

    override suspend fun startSegment(segment: PotionEffectSegment) {
        super.startSegment(segment)
        state = player.state(EffectStateProvider(segment.potionEffectType))

        SYNC.switchContext {
            var flags: Byte = 0
            if(segment.ambient) flags = flags or Potion.AMBIENT_FLAG
            if(segment.particles) flags = flags or Potion.PARTICLES_FLAG
            if(segment.icon) flags = flags or Potion.ICON_FLAG

            player.addEffect(
                Potion(
                    segment.potionEffectType,
                    segment.strength.toByte(),
                    10000000,
                    flags
                )
            )
        }
    }

    override suspend fun stopSegment(segment: PotionEffectSegment) {
        super.stopSegment(segment)
        restoreState()
    }

    private suspend fun restoreState() {
        val state = state ?: return
        this.state = null
        SYNC.switchContext {
            player.restore(state)
        }
    }

    override suspend fun teardown() {
        super.teardown()

        if (state != null) {
            restoreState()
        }
    }
}
