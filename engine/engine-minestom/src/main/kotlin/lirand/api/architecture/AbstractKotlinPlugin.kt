package lirand.api.architecture

import com.typewritermc.engine.paper.adapt.JavaPlugin
import lirand.api.LirandAPI

abstract class AbstractKotlinPlugin : JavaPlugin() {
    override fun onEnable() {
		try {
			LirandAPI.register(this)
		} catch (_: IllegalStateException) {}
	}
}