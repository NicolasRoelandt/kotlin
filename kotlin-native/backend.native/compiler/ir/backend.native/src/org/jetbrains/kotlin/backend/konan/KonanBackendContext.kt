/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.descriptors.KonanSharedVariablesManager
import org.jetbrains.kotlin.backend.konan.ir.KonanIr
import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.getPackageFragment

internal interface KonanBackendContext : CommonBackendContext {
    val messageCollector: MessageCollector

    override val ir: KonanIr

    override val builtIns: KonanBuiltIns

    val lazyValues: MutableMap<LazyMember<*>, Any?>

    fun <T> getValue(member: LazyMember<T>): T =
            @Suppress("UNCHECKED_CAST") (lazyValues.getOrPut(member, { member.initializer(this) }) as T)

    fun <T> setValue(member: LazyVarMember<T>, newValue: T) {
        lazyValues[member] = newValue
    }
}

internal abstract class KonanBackendContextAbstract(val config: KonanConfig) : KonanBackendContext {
    abstract override val builtIns: KonanBuiltIns

    abstract override val ir: KonanIr

    override val scriptMode: Boolean = false

    override val sharedVariablesManager by lazy {
        // Creating lazily because builtIns module seems to be incomplete during `link` test;
        // TODO: investigate this.
        KonanSharedVariablesManager(this)
    }

    override val messageCollector: MessageCollector
        get() = config.configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

    override fun report(element: IrElement?, irFile: IrFile?, message: String, isError: Boolean) {
        val location = element?.getCompilerMessageLocation(irFile ?: error("irFile should be not null for $element"))
        this.messageCollector.report(
                if (isError) CompilerMessageSeverity.ERROR else CompilerMessageSeverity.WARNING,
                message, location
        )
    }

    override val internalPackageFqn = KonanFqNames.internalPackageName

    override val mapping: NativeMapping = NativeMapping()

    override val irFactory: IrFactory = IrFactoryImpl
}

internal fun IrElement.getCompilerMessageLocation(containingFile: IrFile): CompilerMessageLocation? =
        createCompilerMessageLocation(containingFile, this.startOffset, this.endOffset)

internal fun IrBuilderWithScope.getCompilerMessageLocation(): CompilerMessageLocation? {
    val declaration = this.scope.scopeOwnerSymbol.owner as? IrDeclaration ?: return null
    val file = declaration.getPackageFragment() as? IrFile ?: return null
    return createCompilerMessageLocation(file, startOffset, endOffset)
}

private fun createCompilerMessageLocation(containingFile: IrFile, startOffset: Int, endOffset: Int): CompilerMessageLocation? {
    val sourceRangeInfo = containingFile.fileEntry.getSourceRangeInfo(startOffset, endOffset)
    return CompilerMessageLocation.create(
            path = sourceRangeInfo.filePath,
            line = sourceRangeInfo.startLineNumber + 1,
            column = sourceRangeInfo.startColumnNumber + 1,
            lineContent = null // TODO: retrieve the line content.
    )
}
