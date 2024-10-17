package com.typewritermc.engine.paper.adapt

interface Plugin {
    val version: String
    var isEnabled: Boolean

    fun onLoad()
    fun onUnload()
    fun onEnable()
    fun onDisable()
}
