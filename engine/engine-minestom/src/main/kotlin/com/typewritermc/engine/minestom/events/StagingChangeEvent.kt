package com.typewritermc.engine.minestom.events

import com.typewritermc.engine.minestom.entry.StagingState
import net.minestom.server.event.Event

data class StagingChangeEvent(val newState: StagingState) : Event