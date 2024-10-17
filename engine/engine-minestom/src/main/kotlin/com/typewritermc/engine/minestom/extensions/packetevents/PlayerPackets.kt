package com.typewritermc.engine.minestom.extensions.packetevents

import com.typewritermc.core.utils.point.toVector
import com.typewritermc.engine.minestom.adapt.Location
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.server.play.CameraPacket
import net.minestom.server.network.packet.server.play.EntityAnimationPacket

fun Player.spectateEntity(entity: Entity) = setCamera(entityId = entity.entityId)

fun Player.stopSpectatingEntity() = setCamera(entityId = entityId)

private fun Player.setCamera(entityId: Int) = sendPacket(CameraPacket(entityId))

enum class ArmSwing {
    LEFT, RIGHT, BOTH;

    val swingLeft: Boolean get() = this == LEFT || this == BOTH
    val swingRight: Boolean get() = this == RIGHT || this == BOTH
}

fun Player.swingArm(entityId: Int, armSwing: ArmSwing) {
    if (armSwing.swingLeft) {
        EntityAnimationPacket(entityId, EntityAnimationPacket.Animation.SWING_OFF_HAND)
    }
    if (armSwing.swingRight) {
        EntityAnimationPacket(entityId, EntityAnimationPacket.Animation.SWING_MAIN_ARM)
    }
}

fun Location.toPacketLocation() = this
fun ItemStack.toPacketItem() = this
fun Location.toVector3i() = toVector()
fun Location.toVector3d() = toVector()
