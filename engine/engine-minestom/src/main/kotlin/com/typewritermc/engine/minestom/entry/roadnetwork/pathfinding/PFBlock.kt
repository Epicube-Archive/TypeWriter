package com.typewritermc.engine.minestom.entry.roadnetwork.pathfinding

import com.extollit.gaming.ai.path.model.IBlockDescription
import com.extollit.gaming.ai.path.model.IBlockObject
import com.extollit.linalg.immutable.AxisAlignedBBox
import net.minestom.server.collision.Shape
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block
import net.minestom.server.registry.Registry.BlockEntry

class PFBlock(
    val location: Pos,
    val block: Block,
    val data: BlockEntry,
) : IBlockDescription, IBlockObject {
    override fun bounds(): AxisAlignedBBox {
        val collisionShape = data.collisionShape()
        return collisionShape.toAABB()
    }

    override fun isFenceLike(): Boolean {
        if (block.name().lowercase().contains("fence"))
            return true
        if (block.name().lowercase().endsWith("wall"))
            return true
        return false
    }

    override fun isClimbable(): Boolean {
        return block.compare(Block.LADDER) || block.name().lowercase().contains("vine")
    }

    override fun isDoor(): Boolean {
        return block.name().lowercase().endsWith("door")
    }

    override fun isIntractable(): Boolean {
        // TODO: Intractability of blocks
        return false
    }

    override fun isImpeding(): Boolean {
        return block.isSolid
    }

    override fun isFullyBounded(): Boolean {
        val voxelShape = data.collisionShape()

        // FIXME: PROBABLY NOT THE RIGHT WAY TO DO IT ??
        val start = voxelShape.relativeStart()
        val end = voxelShape.relativeEnd()

        return start.x() == 0.0
                && start.y() == 0.0
                && start.z() == 0.0
                && end.x() == 1.0
                && end.y() == 1.0
                && end.z() == 1.0
    }

    override fun isLiquid(): Boolean {
        return data.isLiquid
    }

    override fun isIncinerating(): Boolean {
        return block.compare(Block.LAVA) || block.compare(Block.FIRE) || block.compare(Block.SOUL_FIRE) || block.compare(Block.MAGMA_BLOCK)
    }
}

private fun Shape.toAABB(): AxisAlignedBBox {
    val start = relativeStart()
    val end = relativeEnd()

    // FIXME: PROBABLY NOT THE RIGHT WAY TO DO IT ??
    return AxisAlignedBBox(
        start.x(),
        start.y(),
        start.z(),
        end.x(),
        end.y(),
        end.z(),
    )
}