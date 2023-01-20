/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.api

import org.jetbrains.kotlin.analysis.low.level.api.fir.providers.nullableJavaSymbolProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.FirElementFinder
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.getContainingFile
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.withFirEntry
import org.jetbrains.kotlin.analysis.utils.errors.buildErrorWithAttachment
import org.jetbrains.kotlin.analysis.utils.errors.checkWithAttachmentBuilder
import org.jetbrains.kotlin.analysis.utils.errors.unexpectedElementError
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.synthetic.FirSyntheticProperty
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.diagnostics.ConeDestructuringDeclarationsOnTopLevel
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.name.ClassId

class FirDesignationWithFile(
    path: List<FirRegularClass>,
    target: FirElementWithResolvePhase,
    val firFile: FirFile
) : FirDesignation(
    path,
    target,
) {
    fun toSequenceWithFile(includeTarget: Boolean): Sequence<FirElementWithResolvePhase> = sequence {
        yield(firFile)
        yieldAll(path)
        if (includeTarget) yield(target)
    }
}

open class FirDesignation(
    val path: List<FirRegularClass>,
    val target: FirElementWithResolvePhase,
) {
    val firstNonFileDeclaration: FirElementWithResolvePhase
        get() = path.firstOrNull() ?: target

    fun toSequence(includeTarget: Boolean): Sequence<FirElementWithResolvePhase> = sequence {
        yieldAll(path)
        if (includeTarget) yield(target)
    }
}

private fun collectDesignationPath(target: FirElementWithResolvePhase): List<FirRegularClass>? {
    when (target) {
        is FirSimpleFunction,
        is FirProperty,
        is FirField,
        is FirConstructor,
        is FirEnumEntry,
        is FirPropertyAccessor -> {
            require(target is FirCallableDeclaration)

            if ((target !is FirConstructor && target.symbol.callableId.isLocal) || target.status.visibility == Visibilities.Local) {
                return null
            }

            val containingClassId = target.containingClassLookupTag()?.classId ?: return emptyList()

            if (target.origin == FirDeclarationOrigin.SubstitutionOverride) {
                val originalContainingClassId = target.originalForSubstitutionOverride?.containingClassLookupTag()?.classId
                if (containingClassId == originalContainingClassId) {
                    // Ugly temporary hack for call-site substitution overrides (KTIJ-24004).
                    // Containing class ID from the origin cannot be used, as the origin might be in a different module.
                    return emptyList()
                }
            }

            return collectDesignationPathWithContainingClass(target, containingClassId)
        }

        is FirClassLikeDeclaration -> {
            if (target.isLocal) {
                return null
            }

            val containingClassId = target.symbol.classId.outerClassId ?: return emptyList()
            return collectDesignationPathWithContainingClass(target, containingClassId)
        }

        is FirDanglingModifierList -> {
            val containingClassId = target.containingClass()?.classId ?: return emptyList()
            return collectDesignationPathWithContainingClass(target, containingClassId)
        }

        is FirErrorProperty -> {
            return if (target.diagnostic == ConeDestructuringDeclarationsOnTopLevel) emptyList() else null
        }

        else -> {
            return null
        }
    }
}

private fun collectDesignationPathWithContainingClass(target: FirDeclaration, containingClassId: ClassId): List<FirRegularClass>? {
    if (containingClassId.isLocal) {
        return null
    }

    val useSiteSession = getTargetSession(target)

    fun resolveChunk(classId: ClassId): FirRegularClass {
        val declaration = useSiteSession.firProvider.getFirClassifierByFqName(classId)
            ?: useSiteSession.nullableJavaSymbolProvider?.getClassLikeSymbolByClassId(classId)?.fir
            ?: findKotlinStdlibClass(classId, target)

        check(declaration != null)

        checkWithAttachmentBuilder(declaration is FirRegularClass, { "'FirRegularClass' expected as a containing declaration" }) {
            withFirEntry("containingClassFir", declaration)
        }

        return declaration
    }

    return generateSequence(containingClassId) { it.outerClassId }
        .dropWhile { it.shortClassName.isSpecial }
        .map { resolveChunk(it) }
        .toList()
        .asReversed()
}

private fun getTargetSession(target: FirDeclaration): FirSession {
    if (target is FirSyntheticProperty) {
        return getTargetSession(target.getter)
    }

    if (target is FirCallableDeclaration) {
        val containingSymbol = target.containingClassLookupTag()?.toSymbol(target.moduleData.session)
        if (containingSymbol != null) {
            // Synthetic declarations might have a call site session
            return containingSymbol.moduleData.session
        }
    }

    return target.moduleData.session
}

private fun findKotlinStdlibClass(classId: ClassId, target: FirDeclaration): FirRegularClass? {
    if (!classId.packageFqName.startsWith(StandardNames.BUILT_INS_PACKAGE_NAME)) {
        return null
    }

    val firFile = target.getContainingFile() ?: return null
    return FirElementFinder.findClassifierWithClassId(firFile, classId) as? FirRegularClass
}

fun FirElementWithResolvePhase.collectDesignation(firFile: FirFile): FirDesignationWithFile =
    tryCollectDesignation(firFile) ?: buildErrorWithAttachment("No designation of local declaration") {
        withFirEntry("firFile", firFile)
    }

fun FirElementWithResolvePhase.collectDesignation(): FirDesignation =
    tryCollectDesignation()
        ?: buildErrorWithAttachment("No designation of local declaration") {
            withFirEntry("FirDeclaration", this@collectDesignation)
        }

fun FirElementWithResolvePhase.collectDesignationWithFile(): FirDesignationWithFile =
    tryCollectDesignationWithFile()
        ?: buildErrorWithAttachment("No designation of local declaration") {
            withFirEntry("FirDeclaration", this@collectDesignationWithFile)
        }

fun FirElementWithResolvePhase.tryCollectDesignation(firFile: FirFile): FirDesignationWithFile? =
    collectDesignationPath(this)?.let {
        FirDesignationWithFile(it, this, firFile)
    }

fun FirElementWithResolvePhase.tryCollectDesignation(): FirDesignation? =
    collectDesignationPath(this)?.let {
        FirDesignation(it, this)
    }

fun FirElementWithResolvePhase.tryCollectDesignationWithFile(): FirDesignationWithFile? {
    return when (this) {
        is FirDeclaration -> {
            val path = collectDesignationPath(this) ?: return null
            val firFile = getContainingFile() ?: return null
            FirDesignationWithFile(path, this, firFile)
        }
        is FirFileAnnotationsContainer -> {
            val firFile = getContainingFile() ?: return null
            FirDesignationWithFile(path = emptyList(), this, firFile)
        }
        else -> unexpectedElementError<FirElementWithResolvePhase>(this)
    }
}
