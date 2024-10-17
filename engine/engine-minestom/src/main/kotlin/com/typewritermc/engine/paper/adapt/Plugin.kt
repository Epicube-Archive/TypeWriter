package com.typewritermc.engine.paper.adapt

interface Plugin {
    val version: Any
    var isEnabled: Boolean

    fun onLoad()
    fun onUnload()
    fun onEnable()
    fun onDisable()
}
