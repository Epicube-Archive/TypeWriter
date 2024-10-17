package com.typewritermc.engine.paper.adapt

import org.spongepowered.configurate.ConfigurationNode

interface Plugin {
    val config: ConfigurationNode
    val version: String
    var isEnabled: Boolean

    fun onLoad()
    fun onUnload()
    fun onEnable()
    fun onDisable()

    fun saveConfig()
}
