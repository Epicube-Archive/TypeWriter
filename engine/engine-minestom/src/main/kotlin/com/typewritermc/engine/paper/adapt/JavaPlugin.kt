package com.typewritermc.engine.paper.adapt

import java.io.File
import java.util.logging.Logger

abstract class JavaPlugin : Plugin {
    var logger: Logger = Logger.getLogger("JavaPlugin")
    val dataFolder: File = File("typewriter")

    override var isEnabled: Boolean = false

    override fun onLoad() {

    }

    override fun onUnload() {

    }

    override fun onEnable() {
        isEnabled = true
    }

    override fun onDisable() {
        isEnabled = false
    }
}