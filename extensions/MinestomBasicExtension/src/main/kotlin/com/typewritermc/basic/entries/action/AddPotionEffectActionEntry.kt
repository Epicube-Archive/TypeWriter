package com.typewritermc.basic.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.minestom.entry.Criteria
import com.typewritermc.engine.minestom.entry.Modifier
import com.typewritermc.engine.minestom.entry.TriggerableEntry
import com.typewritermc.engine.minestom.entry.entries.ActionEntry
import com.typewritermc.engine.minestom.utils.ThreadType.SYNC
import com.typewritermc.engine.minestom.utils.toTicks
import net.minestom.server.entity.Player
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import java.time.Duration
import kotlin.experimental.or

@Entry(
    "add_potion_effect",
    "Add a potion effect to the player",
    Colors.RED,
    "fa6-solid:flask-vial"
)
/**
 * The `Add Potion Effect Action` is an action that adds a potion effect to the player.
 *
 * ## How could this be used?
 *
 * This action can be useful in a variety of situations. You can use it to provide players with buffs or debuffs, such as speed or slowness, or to create custom effects.
 */
class AddPotionEffectActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val potionEffect: PotionEffect = PotionEffect.SPEED,
    @Default("10000")
    val duration: Duration = Duration.ofSeconds(10),
    @Default("1")
    val amplifier: Int = 1,
    val ambient: Boolean = false,
    @Default("true")
    val particles: Boolean = true,
    @Help("Whether or not to show the potion effect icon in the player's inventory.")
    @Default("true")
    val icon: Boolean = true,
) : ActionEntry {
    override fun execute(player: Player) {
        super.execute(player)

        var flags: Byte = 0
        if(ambient) flags = flags or Potion.AMBIENT_FLAG
        if(particles) flags = flags or Potion.PARTICLES_FLAG
        if(icon) flags = flags or Potion.ICON_FLAG

        val potion = Potion(potionEffect, amplifier.toByte(), duration.toTicks().toInt(), flags)
        SYNC.launch {
            player.addEffect(potion)
        }
    }
}