/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrReturnTarget
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

/**
 * Generated from: [org.jetbrains.kotlin.ir.generator.IrTree.returnableBlock]
 */
abstract class IrReturnableBlock(
    startOffset: Int,
    endOffset: Int,
    type: IrType,
    statements: MutableList<IrStatement>,
    origin: IrStatementOrigin?,
    override val symbol: IrReturnableBlockSymbol,
) : IrBlock(
    startOffset = startOffset,
    endOffset = endOffset,
    type = type,
    statements = statements,
    origin = origin,
), IrSymbolOwner, IrReturnTarget {

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitReturnableBlock(this, data)
}
