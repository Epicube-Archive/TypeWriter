package lirand.api

import com.typewritermc.engine.paper.adapt.Plugin

class LirandAPI internal constructor(internal val plugin: Plugin) {

    companion object {
        private val _instances = mutableMapOf<Plugin, LirandAPI>()
        val instances: Map<Plugin, LirandAPI> get() = _instances

        fun register(plugin: Plugin): LirandAPI {
            check(plugin !in instances) { "Api for this plugin already initialized." }

            return LirandAPI(plugin)
        }
    }

    init {
        _instances[plugin] = this
    }
}