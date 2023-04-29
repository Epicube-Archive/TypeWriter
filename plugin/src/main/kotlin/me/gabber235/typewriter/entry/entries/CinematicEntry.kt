package me.gabber235.typewriter.entry.entries

import me.gabber235.typewriter.adapters.Tags
import me.gabber235.typewriter.adapters.modifiers.Help
import me.gabber235.typewriter.entry.*
import org.bukkit.entity.Player

@Tags("cinematic")
interface CinematicEntry : Entry {
	@Help("The criteria that must be met before this entry is shown")
	val criteria: List<Criteria>

	fun create(player: Player): CinematicAction
}

infix fun <S : Segment> List<S>.activeSegmentAt(frame: Int) = firstOrNull { it isActiveAt frame }
infix fun <S : Segment> List<S>.canFinishAt(frame: Int): Boolean = all { it canFinishAt frame }

interface Segment {
	val startFrame: Int
	val endFrame: Int
}

infix fun Segment.isActiveAt(frame: Int): Boolean = frame in startFrame..endFrame

infix fun Segment.canFinishAt(frame: Int): Boolean = frame > endFrame

interface CinematicAction {
	/**
	 * Called when the cinematic starts
	 */
	fun setup() {}

	/**
	 * Called every frame
	 */
	fun tick(frame: Int) {}

	/**
	 * Called when the cinematic is finished
	 */
	fun teardown() {}

	/**
	 * Common use case
	 * ```kotlin
	 * override fun canFinish(frame: Int): Boolean = entry.segments canFinishAt frame
	 * ```
	 */
	infix fun canFinish(frame: Int): Boolean
}
