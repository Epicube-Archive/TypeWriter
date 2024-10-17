package com.typewritermc.engine.paper.adapt

import net.kyori.adventure.nbt.BinaryTagIO
import net.minestom.server.item.ItemStack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun ItemStack.serializeAsBytes(): ByteArray {
    val baos = ByteArrayOutputStream()
    BinaryTagIO.writer().write(toItemNBT(), baos)
    return baos.toByteArray()
}

fun deserializeItemFromBytes(bytes: ByteArray): ItemStack {
    val tag = BinaryTagIO.reader().read(ByteArrayInputStream(bytes))
    return ItemStack.fromItemNBT(tag)
}