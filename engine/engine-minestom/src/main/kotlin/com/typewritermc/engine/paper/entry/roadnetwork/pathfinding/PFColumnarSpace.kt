package com.typewritermc.engine.paper.entry.roadnetwork.pathfinding

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList
import com.extollit.gaming.ai.path.model.IColumnarSpace
import com.extollit.gaming.ai.path.model.IInstanceSpace
import com.typewritermc.core.utils.point.World
import com.typewritermc.engine.paper.adapt.type
import net.minestom.server.coordinate.Pos
import net.minestom.server.item.Material
import net.minestom.server.item.Materials
import net.minestom.server.snapshot.ChunkSnapshot


class PFColumnarSpace(
    val world: World,
    val snapshot: ChunkSnapshot,
    val instance: IInstanceSpace,
) : IColumnarSpace {
    private val occlusionFieldList = ColumnarOcclusionFieldList(this)

    override fun blockAt(x: Int, y: Int, z: Int): PFBlock {
        val block = snapshot.getBlock(x, y, z)
        return PFBlock(
            Pos(x.toDouble(), y.toDouble(), z.toDouble(), 0f, 0f),
            block,
            block.registry(),
        )
    }

    override fun metaDataAt(x: Int, y: Int, z: Int): Int = 0
    override fun occlusionFields(): ColumnarOcclusionFieldList = occlusionFieldList
    override fun instance(): IInstanceSpace = instance
}