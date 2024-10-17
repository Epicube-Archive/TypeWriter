package com.typewritermc.engine.minestom.utils

import com.typewritermc.core.utils.point.Position
import com.typewritermc.core.utils.point.World
import com.typewritermc.engine.minestom.adapt.Location
import com.typewritermc.engine.minestom.adapt.WeatherType
import com.typewritermc.engine.minestom.entry.roadnetwork.content.toPacketColor
import com.typewritermc.engine.minestom.logger
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceManager
import net.minestom.server.instance.Weather
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.component.EnchantmentList
import net.minestom.server.item.enchant.Enchantment
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import java.io.File
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

fun Pos.distanceSqrt(other: Location): Double {
    return distanceSqrt(other.position)
}

fun Location.distanceSqrt(other: Location): Double {
    return position.distanceSqrt(other)
}

fun Pos.distanceSqrt(other: Pos): Double {
    return (this as Point).distanceSqrt(other)
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

fun Pos.toCenterLocation(instance: Instance?): Location {
    return toLocation(instance).toCenterLocation()
}

fun Location.asTwPoint(): com.typewritermc.core.utils.point.Point {
    return Position(World(instance?.uniqueId.toString()), x, y, z, yaw, pitch)
}

fun Player.setPlayerWeather(weatherType: WeatherType) {
    sendPackets(when (weatherType) {
        WeatherType.DOWNFALL -> Weather.RAIN.createWeatherPackets()
        WeatherType.CLEAR -> Weather.CLEAR.createWeatherPackets()
    })
}

fun Player.spawnParticle(
    particle: Particle,
    location: Location,
    count: Int,
    offsetX: Double,
    offsetY: Double,
    offsetZ: Double,
    speed: Double
) {
    sendPacket(ParticlePacket(
        particle,
        location.position,
        Vec(offsetX, offsetY, offsetZ),
        speed.toFloat(),
        count
    ))
}

val Player.location: Location
    get() = Location(instance, position)

val Player.world: Instance
    get() = instance

val Player.uniqueId: UUID
    get() = uuid

val Entity.uniqueId: UUID
    get() = uuid

val Pos.up: Pos
    get() = Pos(x, y + 1, z)

val Location.up: Location
    get() = Location(instance, x, y + 1, z, pitch, yaw)

val Location.firstWalkableLocationBelow: Location
    get() = apply {
        var tmp = this
        while (tmp.block?.isSolid == false) {
            tmp = withY(tmp.y - 1)
        }
        // We want to be on top of the block
        return tmp.withY(tmp.y + 1)
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

            player.sendPacket(ParticlePacket(
                Particle.DUST.withProperties(color.toPacketColor(), sqrt(radius / 3).toFloat()),
                true,
                Vec(this.x + x, this.y + y, this.z + z),
                Vec.ZERO,
                0f,
                1
            ))
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

fun ItemStack.unClickable(): ItemStack {
    return with(ItemComponent.ENCHANTMENTS, EnchantmentList(Enchantment.BINDING_CURSE, 1))
        .with(ItemComponent.ENCHANTMENT_GLINT_OVERRIDE, false)
}