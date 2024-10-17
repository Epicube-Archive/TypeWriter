package com.typewritermc.engine.paper.loader

import com.typewritermc.loader.DependencyChecker

class PaperDependencyChecker : DependencyChecker {
    override fun hasDependency(dependency: String): Boolean = false
}