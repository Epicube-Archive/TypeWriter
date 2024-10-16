package lirand.api.extensions.math

import net.minestom.server.coordinate.Pos

/*
 * LOCATION
 */

operator fun Pos.component1() = x
operator fun Pos.component2() = y
operator fun Pos.component3() = z
operator fun Pos.component4() = yaw
operator fun Pos.component5() = pitch

fun Location(x: Number, y: Number, z: Number) = Pos(x.toDouble(), y.toDouble(), z.toDouble())

// extensions
fun Pos.add(x: Number = 0.0, y: Number = 0.0, z: Number = 0.0) = add(x.toDouble(), y.toDouble(), z.toDouble())


val Pos.blockLocation: Pos get() = Location(blockX(), blockY(), blockZ())