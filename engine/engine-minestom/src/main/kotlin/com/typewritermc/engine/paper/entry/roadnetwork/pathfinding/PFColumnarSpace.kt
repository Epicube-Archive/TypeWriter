package com.typewritermc.engine.paper.entry.roadnetwork.pathfinding

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList
import com.extollit.gaming.ai.path.model.IColumnarSpace
import com.extollit.gaming.ai.path.model.IInstanceSpace
import com.typewritermc.core.utils.point.World
import net.minestom.server.coordinate.Pos
import net.minestom.server.snapshot.ChunkSnapshot


class PFColumnarSpace(
    val world: World,
    val snapshot: ChunkSnapshot,
    val instance: IInstanceSpace,
) : IColumnarSpace {
    private val occlusionFieldList = ColumnarOcclusionFieldList(this)

    override fun blockAt(x: Int, y: Int, z: Int): PFBlock = PFBlock(
        Pos(x.toDouble(), y.toDouble(), z.toDouble(), 0f, 0f),
        snapshot.getBlockType(x, y, z),
        snapshot.getBlockData(x, y, z),
    )

    override fun metaDataAt(x: Int, y: Int, z: Int): Int = 0
    override fun occlusionFields(): ColumnarOcclusionFieldList = occlusionFieldList
    override fun instance(): IInstanceSpace = instance
}