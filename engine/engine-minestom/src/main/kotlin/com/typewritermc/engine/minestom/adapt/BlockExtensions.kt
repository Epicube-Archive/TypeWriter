package com.typewritermc.engine.minestom.adapt

import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material

val Block.type: Material?
    get() = registry().material()