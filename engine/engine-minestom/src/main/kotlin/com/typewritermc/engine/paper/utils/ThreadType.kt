package com.typewritermc.engine.paper.utils

import com.github.shynixn.mccoroutine.minestom.asyncDispatcher
import com.github.shynixn.mccoroutine.minestom.launch
import com.github.shynixn.mccoroutine.minestom.minecraftDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import lirand.api.extensions.server.server
import com.typewritermc.engine.paper.plugin

enum class ThreadType {
    SYNC,
    ASYNC,
    DISPATCHERS_ASYNC,
    REMAIN,
    ;

    suspend fun <T> switchContext(block: suspend () -> T): T {
        if (this == REMAIN) {
            return block()
        }

        return withContext(
            when (this) {
                SYNC -> server.minecraftServer!!.minecraftDispatcher
                ASYNC -> server.minecraftServer!!.asyncDispatcher
                DISPATCHERS_ASYNC -> Dispatchers.IO
                else -> throw IllegalStateException("Unknown thread type: $this")
            }
        ) {
            block()
        }
    }

    fun launch(block: suspend () -> Unit): Job {
        if (!plugin.isEnabled) {
            runBlocking {
                block()
            }
            return Job()
        }

        return server.minecraftServer!!.launch(
            when (this) {
                SYNC -> server.minecraftServer!!.minecraftDispatcher
                ASYNC -> server.minecraftServer!!.asyncDispatcher
                DISPATCHERS_ASYNC -> Dispatchers.IO
                REMAIN -> if (server.isPrimaryThread) server.minecraftServer!!.minecraftDispatcher else server.minecraftServer!!.asyncDispatcher
            }
        ) {
            block()
        }
    }
}