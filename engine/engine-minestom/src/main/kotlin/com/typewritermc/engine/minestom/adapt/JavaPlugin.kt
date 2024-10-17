package com.typewritermc.engine.minestom.adapt

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import java.io.File
import java.nio.file.Path
import java.util.logging.Logger

abstract class JavaPlugin : Plugin {
    private val configLoader = GsonConfigurationLoader.builder().path(Path.of("typewriter/config.json")).build();
    override val config: ConfigurationNode = configLoader.load()

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

    override fun saveConfig() {
        configLoader.save(config)
    }
}