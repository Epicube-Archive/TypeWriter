package com.typewritermc.engine.paper.utils

import com.destroystokyo.paper.profile.PlayerProfile
import com.github.retrooper.packetevents.protocol.particle.Particle
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle
import com.typewritermc.engine.paper.adapt.Location
import com.typewritermc.engine.paper.entry.roadnetwork.content.toPacketColor
import com.typewritermc.engine.paper.extensions.packetevents.sendPacketTo
import com.typewritermc.engine.paper.logger
import lirand.api.extensions.server.server
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceManager
import java.io.File
import java.net.MalformedURLException
import java.net.URI
import java.time.Duration
import java.util.*
import kotlin.math.*

operator fun File.get(name: String): File = File(this, name)

fun Event.callEvent() {
    MinecraftServer.getGlobalEventHandler().call(this)
}

val Player.isFloodgate: Boolean
    get() {
        return false
    }

/**
 * Can an entity look at this player?
 */
val Player.isLookable: Boolean
    get() = !this.isDead && this.isOnline && this.gameMode != GameMode.SPECTATOR && !this.isInvisible

fun <T> T?.logErrorIfNull(message: String): T? {
    if (this == null) logger.severe(message)
    return this
}

infix fun <T> Boolean.then(t: T): T? = if (this) t else null


fun Duration.toTicks(): Long = this.toMillis() / 50
operator fun Duration.times(other: Double): Duration = Duration.ofMillis((this.toMillis() * other).roundToLong())

fun Audience.playSound(
    sound: String,
    source: Sound.Source = Sound.Source.MASTER,
    volume: Float = 1.0f,
    pitch: Float = 1.0f
) = playSound(Sound.sound(Key.key(sound), source, volume, pitch))

fun InstanceManager.findGlobalPlayerByUuid(uuid: UUID): Player? {
    return instances
        .flatMap { it.players }
        .find { it.uuid == uuid }
}

fun Point.distanceSqrt(other: Point): Double {
    val dx = x() - other.x()
    val dy = y() - other.y()
    val dz = z() - other.z()
    return dx * dx + dy * dy + dz * dz
}

fun Location.distanceSqrt(other: Point): Double {
    return position.distanceSqrt(other)
}

fun Pos.distanceSqrt(other: Pos): Double {
    val dx = x - other.x
    val dy = y - other.y
    val dz = z - other.z
    return dx * dx + dy * dy + dz * dz
}

fun Pos.lerp(other: Pos, amount: Double): Pos {
    val percentage = amount.coerceIn(0.0, 1.0)
    val x = this.x + (other.x - this.x) * percentage
    val y = this.y + (other.y - this.y) * percentage
    val z = this.z + (other.z - this.z) * percentage
    return Pos(x, y, z)
}

fun Location.lerp(other: Location, amount: Double): Location {
    return Location(instance, position.lerp(other.position, amount))
}

fun Pos.toLocation(instance: Instance?): Location {
    return Location(instance, this)
}

fun Location.toCenterLocation(): Location {
    return withX(blockX + 0.5)
        .withY(blockY + 0.5)
        .withZ(blockZ + 0.5)
}

val Player.location: Location
    get() = Location(instance, position)

val Pos.up: Pos
    get() = Pos(x, y + 1, z)

val Location.up: Location
    get() = Location(instance, x, y + 1, z, pitch, yaw)

val Pos.firstWalkableLocationBelow: Pos
    get() = clone().apply {
        while (block.isPassable) y--
        // We want to be on top of the block
        y++
    }

operator fun Pos.component1(): Double = x
operator fun Pos.component2(): Double = y
operator fun Pos.component3(): Double = z

fun Location.particleSphere(
    player: Player,
    radius: Double,
    color: Color,
    phiDivisions: Int = 16,
    thetaDivisions: Int = 8,
) {
    var phi = 0.0
    while (phi < Math.PI) {
        phi += Math.PI / phiDivisions
        var theta = 0.0
        while (theta < 2 * Math.PI) {
            theta += Math.PI / thetaDivisions
            val x = radius * sin(phi) * cos(theta)
            val y = radius * cos(phi)
            val z = radius * sin(phi) * sin(theta)

            WrapperPlayServerParticle(
                Particle(
                    ParticleTypes.DUST,
                    ParticleDustData(sqrt(radius / 3).toFloat(), color.toPacketColor())
                ),
                true,
                Vector3d(this.x + x, this.y + y, this.z + z),
                Vector3f.zero(),
                0f,
                1
            ) sendPacketTo player
        }
    }
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }

    return round(this * multiplier) / multiplier
}

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }

    return (round(this * multiplier) / multiplier).toFloat()
}

val Int.digits: Int
    get() = if (this == 0) 1 else log10(abs(this.toDouble())).toInt() + 1


val String.lineCount: Int
    get() = this.count { it == '\n' } + 1

val <T : Any> Optional<T>?.optional: Optional<T> get() = Optional.ofNullable(this?.orElse(null))
val <T : Any> T?.optional: Optional<T> get() = Optional.ofNullable(this)

var ItemMeta.loreString: String?
    get() = lore()?.joinToString("\n") { it.asMini() }
    set(value) {
        lore(value?.split("\n")?.map { "<!i><white>$it".asMini() })
    }

var ItemMeta.name: String?
    get() = if (hasDisplayName()) displayName()?.asMini() else null
    set(value) = displayName(if (!value.isNullOrEmpty()) "<!i>$value".asMini() else Component.text(" "))

fun ItemMeta.unClickable(): ItemMeta {
    addEnchant(Enchantment.BINDING_CURSE, 1, true)
    addItemFlags(ItemFlag.HIDE_ENCHANTS)
    return this
}

private val RANDOM_UUID =
    UUID.fromString("92864445-51c5-4c3b-9039-517c9927d1b4") // We reuse the same "random" UUID all the time

private fun getProfile(url: String): PlayerProfile {
    val profile: PlayerProfile = server.createProfile(RANDOM_UUID) // Get a new player profile
    val textures: PlayerTextures = profile.textures
    textures.skin = try {
        // The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
        URI(url).toURL()
    } catch (exception: MalformedURLException) {
        throw RuntimeException("Invalid URL", exception)
    }
    profile.setTextures(textures) // Set the textures back to the profile
    return profile
}

fun SkullMeta.applySkinUrl(url: String) {
    playerProfile = getProfile(url)
}
