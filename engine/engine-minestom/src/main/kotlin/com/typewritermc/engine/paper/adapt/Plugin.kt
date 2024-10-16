package com.typewritermc.engine.paper.adapt

interface Plugin {
    var isEnabled: Boolean

    fun onLoad()
    fun onUnload()
    fun onEnable()
    fun onDisable()
}
