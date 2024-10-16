package com.typewritermc.engine.paper.adapt

import net.minestom.server.instance.Instance
import net.minestom.server.tag.Tag
import java.util.UUID

private val instanceUidTag: Tag<UUID> = Tag.UUID("uid")

val Instance.uid: UUID get() {
    return getTag(instanceUidTag)
}