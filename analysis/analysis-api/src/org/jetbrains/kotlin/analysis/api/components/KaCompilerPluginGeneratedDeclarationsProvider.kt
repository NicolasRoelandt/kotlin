/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.components

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.lifetime.KaLifetimeOwner
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule
import org.jetbrains.kotlin.analysis.api.scopes.KaScope

/**
 * Provides declarations generated by compiler plugins.
 */
@KaExperimentalApi
public interface KaCompilerPluginGeneratedDeclarationsProvider {
    /**
     * A [KaCompilerPluginGeneratedDeclarations] for [this] module.
     *
     * Important: the result **does not** include the generated declarations for the
     * dependencies of [this] module.
     */
    public val KaModule.compilerPluginGeneratedDeclarations: KaCompilerPluginGeneratedDeclarations
}

/**
 * Represents declarations generated by compiler plugins.
 */
@KaExperimentalApi
public interface KaCompilerPluginGeneratedDeclarations : KaLifetimeOwner {

    /**
     * A [KaScope] containing top-level declarations generated by compiler plugins.
     */
    public val topLevelDeclarationsScope: KaScope
}