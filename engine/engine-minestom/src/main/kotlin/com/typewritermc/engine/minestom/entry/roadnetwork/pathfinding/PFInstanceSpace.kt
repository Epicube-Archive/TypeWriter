package com.typewritermc.engine.minestom.entry.roadnetwork.pathfinding

import com.extollit.gaming.ai.path.model.IBlockObject
import com.extollit.gaming.ai.path.model.IInstanceSpace
import com.typewritermc.core.utils.point.World
import net.minestom.server.instance.Instance
import net.minestom.server.snapshot.InstanceSnapshot
import net.minestom.server.snapshot.SnapshotUpdater
import java.util.concurrent.ConcurrentHashMap

class PFInstanceSpace(val world: Instance) : IInstanceSpace {
    private val chunkSpaces = ConcurrentHashMap<Long, PFColumnarSpace>()

    override fun blockObjectAt(x: Int, y: Int, z: Int): IBlockObject {
        val chunkX = x shr 4
        val chunkZ = z shr 4

        val columnarSpace = columnarSpaceAt(chunkX, chunkZ)
        val relativeX = x and 15
        val relativeY = y and 15
        val relativeZ = z and 15
        return columnarSpace.blockAt(relativeX, relativeY, relativeZ)
    }

    override fun columnarSpaceAt(cx: Int, cz: Int): PFColumnarSpace {
        val key = cx.toLong() and 4294967295L or ((cz.toLong() and 4294967295L) shl 32)
        return chunkSpaces.computeIfAbsent(key) {
            //val chunk = world.loadChunk(cx, cz)
            world.loadChunk(cx, cz) // guarantee chunk is loaded ?
            val chunk = SnapshotUpdater.update<InstanceSnapshot>(world).chunk(cx, cz)
            PFColumnarSpace(World(world.uniqueId.toString()), chunk!!, this)
        }
    }
}