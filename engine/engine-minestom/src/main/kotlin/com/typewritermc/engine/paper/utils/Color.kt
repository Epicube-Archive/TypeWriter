package com.typewritermc.engine.paper.utils

import net.kyori.adventure.util.RGBLike

class Color(
    val color: Int,
): RGBLike {
    companion object {
        val RED: Color = fromRGB(255, 0, 0)
        val GREEN: Color = fromRGB(0, 255, 0)
        val BLUE: Color = fromRGB(0, 0, 255)
        val WHITE: Color = fromRGB(255, 255, 255)
        val BLACK: Color = fromRGB(0, 0, 0)

        fun fromHex(hex: String): Color {
            val color = hex.removePrefix("#").toInt(16)
            return Color(color)
        }

        fun fromARGB(alpha: Int, red: Int, green: Int, blue: Int): Color {
            var color = alpha
            color = (color shl 8) + red
            color = (color shl 8) + green
            color = (color shl 8) + blue
            return Color(color)
        }

        fun fromRGB(red: Int, green: Int, blue: Int): Color {
            return fromARGB(255, red, green, blue)
        }

        val BLACK_BACKGROUND = Color(0x40000000)
    }

    val alpha: Int get() = (color shr 24) and 0xFF
    val red: Int get() = (color shr 16) and 0xFF
    val green: Int get() = (color shr 8) and 0xFF
    val blue: Int get() = color and 0xFF

    fun toMinestomColor(): net.minestom.server.color.AlphaColor {
        return net.minestom.server.color.AlphaColor(alpha, red, green, blue)
    }

    override fun red(): Int = red
    override fun green(): Int = green
    override fun blue(): Int = blue
}