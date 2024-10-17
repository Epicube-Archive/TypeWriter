package com.typewritermc.engine.minestom.utils

import com.typewritermc.core.utils.point.*
import com.typewritermc.core.utils.point.Vector
import com.typewritermc.engine.minestom.adapt.Location
import com.typewritermc.engine.minestom.adapt.uid
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import java.util.*

fun Vec.toVector(): Vector {
    return Vector(x, y, z)
}

fun Point.toPacketVector3f() = Vec(x, y, z)
fun Point.toPacketVector3d() = Vec(x, y, z)
fun Point.toPacketVector3i() = Vec(blockX.toDouble(), blockY.toDouble(), blockZ.toDouble())
fun Point.toBukkitVector() = Vec(x, y, z)

fun World.toBukkitWorld() = MinecraftServer.getInstanceManager().getInstance(UUID.fromString(identifier))
    ?: throw IllegalArgumentException("Could not find instance '$identifier' for location, and no default world available.")

fun Position.toMinestomPos() = Pos(x, y, z, yaw, pitch)
fun Position.toBukkitLocation() = Location(world.toBukkitWorld(), x, y, z, yaw, pitch)
fun Position.toPacketLocation() = toBukkitLocation().toPacketLocation()

fun Location.toPosition(): Position = Position(World(world?.uid.toString()), x, y, z, yaw, pitch)
fun Location.toPacketLocation() = this

fun Location.toCoordinate() = Coordinate(x, y, z, yaw, pitch)