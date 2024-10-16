package com.typewritermc.engine.paper.events

import com.typewritermc.engine.paper.entry.StagingState
import net.minestom.server.event.Event

data class StagingChangeEvent(val newState: StagingState) : Event