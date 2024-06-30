/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.optimizations

import org.jetbrains.kotlin.backend.common.*
import org.jetbrains.kotlin.backend.common.ir.isPure
import org.jetbrains.kotlin.backend.common.lower.LoweredStatementOrigins
import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.optimizations.LivenessAnalysis
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION
import org.jetbrains.kotlin.backend.konan.DirectedGraphCondensationBuilder
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.getMemoryUsage
import org.jetbrains.kotlin.backend.konan.ir.isAny
import org.jetbrains.kotlin.backend.konan.ir.isArray
import org.jetbrains.kotlin.backend.konan.ir.isVirtualCall
import org.jetbrains.kotlin.backend.konan.lower.erasure
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrReturnableBlockSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrStarProjectionImpl
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.objcinterop.isExternalObjCClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.sumByLong
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

internal class BackendInlinerOptions(val inlineBoxUnbox: Boolean)

internal class BackendInliner(
        val generationState: NativeGenerationState,
        val moduleDFG: ModuleDFG,
        val devirtualizedCallSites: MutableMap<DataFlowIR.Node.VirtualCall, DevirtualizationAnalysis.DevirtualizedCallSite>,
        val callGraph: CallGraph,
        val options: BackendInlinerOptions,
) {
    private val context = generationState.context
    private val symbols = context.ir.symbols
    private val noInline = symbols.noInline
    private val string = symbols.string
    private val throwable = symbols.throwable
    private val invokeSuspendFunction = symbols.invokeSuspendFunction

    private val rootSet = callGraph.rootSet

    private var maxMemoryUsage = getMemoryUsage()

    private fun updateMemoryUsage() {
        val currentMemoryUsage = getMemoryUsage()
        if (maxMemoryUsage < currentMemoryUsage)
            maxMemoryUsage = currentMemoryUsage
    }

    private data class Specimen(val genome: BitSet, val score: Float)

    private class FunctionScoreIngredients {
        var needsOwnFrame: Boolean = false
        var framesTraversalComplexity: Float = 0.0f
        var irSize: Int = 0
    }

    private data class CallSiteInfo(val id: Int, val depth: Int)

    fun run2(): Map<IrFunction, Set<IrFunction>> {
        val nodeDepths = mutableMapOf<DataFlowIR.Node, Int>()
        for (functionSymbol in callGraph.directEdges.keys) {
            val function = moduleDFG.functions[functionSymbol]!!
            for (scope in function.body.allScopes)
                for (node in scope.nodes) {
                    if (node !is DataFlowIR.Node.Scope)
                        nodeDepths[node] = scope.depth
                }
        }

        // Compute call sites ids.
        var id = 0
        val callSiteInfos = mutableMapOf<CallGraphNode.CallSite, CallSiteInfo>()
        for (callGraphNode in callGraph.directEdges.values) {
            callGraphNode.callSites
                    .filter { !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee) }
                    .forEach { callSiteInfos[it] = CallSiteInfo(id++, nodeDepths[it.node]!!) }
        }

        // Find backward edges.
        val forbiddenToInline = BitSet()
        val computationStates = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, ComputationState>()
        val stack = rootSet.toMutableList()
        for (root in stack)
            computationStates[root] = ComputationState.NEW
        while (stack.isNotEmpty()) {
            val functionSymbol = stack.peek()!!
            val state = computationStates[functionSymbol]!!
            val callSites = callGraph.directEdges[functionSymbol]!!.callSites.filter {
                !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee)
            }
            when (state) {
                ComputationState.NEW -> {
                    computationStates[functionSymbol] = ComputationState.PENDING
                    for (callSite in callSites) {
                        val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
                        if (computationStates[calleeSymbol] == null || computationStates[calleeSymbol] == ComputationState.NEW) {
                            computationStates[calleeSymbol] = ComputationState.NEW
                            stack.push(calleeSymbol)
                        }
                    }
                }

                ComputationState.PENDING -> {
                    stack.pop()
                    computationStates[functionSymbol] = ComputationState.DONE
                    for (callSite in callSites) {
                        val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
                        val callee = moduleDFG.functions[calleeSymbol]!!
                        var isALoop = false
                        callee.body.forEachNonScopeNode { node ->
                            if (node is DataFlowIR.Node.Call && node.callee == calleeSymbol)
                                isALoop = true
                        }
                        if (computationStates[calleeSymbol] != ComputationState.DONE || isALoop)
                            forbiddenToInline.set(callSiteInfos[callSite]!!.id)

                        val calleeIrFunction = calleeSymbol.irFunction ?: continue
                        if ((calleeIrFunction.origin == DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION && !options.inlineBoxUnbox)
                                || calleeIrFunction.hasAnnotation(noInline)
                                || (calleeIrFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.hasAnnotation(noInline) == true
                                || (calleeIrFunction as? IrSimpleFunction)?.overrides(invokeSuspendFunction.owner) == true // TODO: Is it worth trying to support?
                                || (calleeIrFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.let {
                                    it.parentClassOrNull?.isSingleFieldValueClass == true || it.backingField != null
                                } == true
                                || (calleeIrFunction as? IrConstructor)?.constructedClass?.let { it.isArray || it.symbol == string } == true
                                || (calleeIrFunction as? IrConstructor)?.constructedClass?.getAllSuperclasses()?.contains(throwable.owner) == true
                        ) {
                            forbiddenToInline.set(callSiteInfos[callSite]!!.id)
                        }
                    }
                }

                ComputationState.DONE -> {
                    stack.pop()
                }
            }
        }

        val totalIrSize = callGraph.directEdges.keys.sumByLong { functionSymbol ->
            moduleDFG.functions[functionSymbol]!!.body.allScopes.sumOf { it.nodes.size }.toLong()
        }

        val condensation = DirectedGraphCondensationBuilder(callGraph).build()

        val chromosomesCount = min(callGraph.directEdges.size, 23)
        val chromosomeLength = callSiteInfos.size / chromosomesCount
        val chromosomeEnds = IntArray(chromosomesCount + 1)
        chromosomeEnds[chromosomesCount] = callSiteInfos.size
        var chromosomeId = 0
        var callSitesCount = 0
        for (callGraphNode in callGraph.directEdges.values) {
            val count = callGraphNode.callSites.count { !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee) }
            if (callSitesCount + count > chromosomeLength * (chromosomeId + 1)) {
                chromosomeEnds[++chromosomeId] = callSitesCount
            }
            callSitesCount += count
            if (chromosomeId == chromosomesCount - 1) break
        }
        assert(chromosomeId == chromosomesCount - 1)

        println("ZZZ: $chromosomesCount $chromosomeLength")
        println("    ${chromosomeEnds.contentToString()}")

        val genomeSize = callSiteInfos.size

        val loopsPenalty = 5
        val populationSize = 1_000
        val generations = 250//1_000
        val mutationsCount = max(1, genomeSize / 1_000)
        val random = Random(0x42424242)

        fun pow(x: Float, k: Int) = kotlin.math.exp(k * kotlin.math.ln(x))

        fun computeScore(genome: BitSet): Float {
            val functionsScoreIngredients = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, FunctionScoreIngredients>()
            for (multiNode in condensation.topologicalOrder.reversed()) {
                val nodes = multiNode.nodes.filter { moduleDFG.functions.containsKey(it) }.toMutableSet()
                for (functionSymbol in nodes) {
//                    val function = moduleDFG.functions[functionSymbol]!!
//                    val needsOwnFrame = function.body.allScopes.any { scope ->
//                        scope.nodes.any {
//                            it is DataFlowIR.Node.Variable
//                                    && it.kind == DataFlowIR.VariableKind.Ordinary
//                                    && it.values.size > 1
//                        }
//                    }
                    functionsScoreIngredients[functionSymbol] = FunctionScoreIngredients()//.also { it.needsOwnFrame = needsOwnFrame }
                }
                var counter = 0
                var hasRecursion = false
                do {
                    for (functionSymbol in nodes) {
                        val function = moduleDFG.functions[functionSymbol]!!
                        val scoreIngredients = functionsScoreIngredients[functionSymbol]!!
                        scoreIngredients.irSize = function.body.allScopes.sumOf { it.nodes.size }
                        var framesTraversalComplexity = 0.0f
                        callGraph.directEdges[functionSymbol]!!.callSites
                                .filter { !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee) }
                                .forEach { callSite ->
                                    val (gene, depth) = callSiteInfos[callSite]!!
                                    if (callSite.actualCallee in nodes)
                                        hasRecursion = true
                                    val calleeIngredients = functionsScoreIngredients[callSite.actualCallee]!!
                                    if (genome[gene]) {
                                        scoreIngredients.irSize += calleeIngredients.irSize - 1
                                        //scoreIngredients.needsOwnFrame = scoreIngredients.needsOwnFrame || calleeIngredients.needsOwnFrame
                                    }
                                    // The callee's frame moves to the caller's.
                                    val discount = if (genome[gene]/* && calleeIngredients.needsOwnFrame*/) 1 else 0
                                    val discountedComplexity = calleeIngredients.framesTraversalComplexity - discount
                                    framesTraversalComplexity += max(0.0f, discountedComplexity) * pow(loopsPenalty * 1.0f, depth)
                                }
                        //if (scoreIngredients.needsOwnFrame)
                            framesTraversalComplexity += 1
                        scoreIngredients.framesTraversalComplexity = framesTraversalComplexity
                    }
                    ++counter
                } while (hasRecursion && counter < loopsPenalty)
            }

            val framesTraversalComplexity = rootSet.sumOf { functionsScoreIngredients[it]!!.framesTraversalComplexity.toDouble() }
            val irSwellingFactor = callGraph.directEdges.keys.sumByLong { functionsScoreIngredients[it]!!.irSize.toLong() } * 1.0 / totalIrSize
            return (framesTraversalComplexity * kotlin.math.sqrt(irSwellingFactor)).toFloat()
        }

        @Suppress("NAME_SHADOWING")
        fun crossingOver(genome1: BitSet, genome2: BitSet): BitSet {
            val offspring = BitSet(genomeSize)
            for (chromosomeId in 0..<chromosomesCount) {
                val chromosomeStart = chromosomeEnds[chromosomeId]
                val chromosomeEnd = chromosomeEnds[chromosomeId + 1]
                val knotPlace = random.nextInt(chromosomeStart, chromosomeEnd)
                for (gene in chromosomeStart..<chromosomeEnd)
                    offspring[gene] = if (gene <= knotPlace) genome1[gene] else genome2[gene]
            }
            return offspring
        }

        println("No inline score: ${computeScore(BitSet(genomeSize))}")
        println("All inline score: ${computeScore(BitSet(genomeSize).apply { (0..<genomeSize).forEach { set(it) }; andNot(forbiddenToInline) })}")

        val population = MutableList(populationSize) {
            val genome = BitSet(genomeSize)
            for (gene in 0..<genomeSize)
                genome[gene] = random.nextBoolean()
            genome.andNot(forbiddenToInline)
            Specimen(genome, computeScore(genome))
        }
        var bestScore = Float.MAX_VALUE
        var bestGenome = BitSet(genomeSize)
        population.forEach { specimen ->
            if (bestScore > specimen.score) {
                bestScore = specimen.score
                bestGenome = specimen.genome
            }
        }
        val startTime = System.nanoTime()
        for (generation in 0..<generations) {
            println("Generation $generation: $bestScore. Elapsed = ${(System.nanoTime() - startTime) / 1e9}")
            for (i in 0..<populationSize) {
                val parent1Index = random.nextInt(populationSize)
                var parent2Index: Int
                do {
                    parent2Index = random.nextInt(populationSize)
                } while (parent2Index == parent1Index)
                val offspring = crossingOver(population[parent1Index].genome, population[parent2Index].genome)
                for (j in 0..<mutationsCount) {
                    val gene = random.nextInt(genomeSize)
                    offspring[gene] = !offspring[gene]
                }
                offspring.andNot(forbiddenToInline)
                population.add(Specimen(offspring, computeScore(offspring)))
            }
            population.sortBy { it.score }
            for (i in 0..<populationSize)
                population.removeLast()
            if (bestScore > population[0].score) {
                bestScore = population[0].score
                bestGenome = population[0].genome
            }
        }

        val result = mutableMapOf<IrFunction, Set<IrFunction>>()
        for ((functionSymbol, callGraphNode) in callGraph.directEdges) {
            val irFunction = functionSymbol.irFunction ?: continue
            result[irFunction] = callGraphNode.callSites
                    .filter {
                        !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee)
                                && bestGenome[callSiteInfos[it]!!.id]
                    }
                    .map { it.actualCallee.irFunction!! }
                    .toSet()
        }
        return result
    }

    fun run3() {
        val nodeDepths = mutableMapOf<DataFlowIR.Node, Int>()
        for (functionSymbol in callGraph.directEdges.keys) {
            val function = moduleDFG.functions[functionSymbol]!!
            for (scope in function.body.allScopes)
                for (node in scope.nodes) {
                    if (node !is DataFlowIR.Node.Scope)
                        nodeDepths[node] = scope.depth
                }
        }

        // Compute call sites ids.
        var id = 0
        val callSiteInfos = mutableMapOf<CallGraphNode.CallSite, CallSiteInfo>()
        for (callGraphNode in callGraph.directEdges.values) {
            callGraphNode.callSites
                    .filter { !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee) }
                    .forEach { callSiteInfos[it] = CallSiteInfo(id++, nodeDepths[it.node]!!) }
        }

        // Find backward edges.
        val forbiddenToInline = BitSet()
        val computationStates = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, ComputationState>()
        val stack = rootSet.toMutableList()
        for (root in stack)
            computationStates[root] = ComputationState.NEW
        while (stack.isNotEmpty()) {
            val functionSymbol = stack.peek()!!
            val state = computationStates[functionSymbol]!!
            val callSites = callGraph.directEdges[functionSymbol]!!.callSites.filter {
                !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee)
            }
            when (state) {
                ComputationState.NEW -> {
                    computationStates[functionSymbol] = ComputationState.PENDING
                    for (callSite in callSites) {
                        val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
                        if (computationStates[calleeSymbol] == null || computationStates[calleeSymbol] == ComputationState.NEW) {
                            computationStates[calleeSymbol] = ComputationState.NEW
                            stack.push(calleeSymbol)
                        }
                    }
                }

                ComputationState.PENDING -> {
                    stack.pop()
                    computationStates[functionSymbol] = ComputationState.DONE
                    for (callSite in callSites) {
                        val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
                        if (computationStates[calleeSymbol] != ComputationState.DONE)
                            forbiddenToInline.set(callSiteInfos[callSite]!!.id)

                        val calleeIrFunction = calleeSymbol.irFunction ?: continue
                        if ((calleeIrFunction.origin == DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION && !options.inlineBoxUnbox)
                                || calleeIrFunction.hasAnnotation(noInline)
                                || (calleeIrFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.hasAnnotation(noInline) == true
                                || (calleeIrFunction as? IrSimpleFunction)?.overrides(invokeSuspendFunction.owner) == true // TODO: Is it worth trying to support?
                                || (calleeIrFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.let {
                                    it.parentClassOrNull?.isSingleFieldValueClass == true || it.backingField != null
                                } == true
                                || (calleeIrFunction as? IrConstructor)?.constructedClass?.let { it.isArray || it.symbol == string } == true
                                || (calleeIrFunction as? IrConstructor)?.constructedClass?.getAllSuperclasses()?.contains(throwable.owner) == true
                        ) {
                            forbiddenToInline.set(callSiteInfos[callSite]!!.id)
                        }
                    }
                }

                ComputationState.DONE -> {
                    stack.pop()
                }
            }
        }

        val totalIrSize = callGraph.directEdges.keys.sumByLong { functionSymbol ->
            moduleDFG.functions[functionSymbol]!!.body.allScopes.sumOf { it.nodes.size }.toLong()
        }

        val condensation = DirectedGraphCondensationBuilder(callGraph).build()

        val loopsPenalty = 5
        val random = Random(0x42424242)

        fun pow(x: Float, k: Int) = kotlin.math.exp(k * kotlin.math.ln(x))

        fun computeScore(genome: BitSet): Float {
            val functionsScoreIngredients = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, FunctionScoreIngredients>()
            for (multiNode in condensation.topologicalOrder.reversed()) {
                val nodes = multiNode.nodes.filter { moduleDFG.functions.containsKey(it) }.toMutableSet()
                for (functionSymbol in nodes) {
//                    val function = moduleDFG.functions[functionSymbol]!!
//                    val needsOwnFrame = function.body.allScopes.any { scope ->
//                        scope.nodes.any {
//                            it is DataFlowIR.Node.Variable
//                                    && it.kind == DataFlowIR.VariableKind.Ordinary
//                                    && it.values.size > 1
//                        }
//                    }
                    functionsScoreIngredients[functionSymbol] = FunctionScoreIngredients()//.also { it.needsOwnFrame = needsOwnFrame }
                }
                var counter = 0
                var hasRecursion = false
                do {
                    for (functionSymbol in nodes) {
                        val function = moduleDFG.functions[functionSymbol]!!
                        val scoreIngredients = functionsScoreIngredients[functionSymbol]!!
                        scoreIngredients.irSize = function.body.allScopes.sumOf { it.nodes.size }
                        var framesTraversalComplexity = 0.0f
                        callGraph.directEdges[functionSymbol]!!.callSites
                                .filter { !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee) }
                                .forEach { callSite ->
                                    val (gene, depth) = callSiteInfos[callSite]!!
                                    if (callSite.actualCallee in nodes)
                                        hasRecursion = true
                                    val calleeIngredients = functionsScoreIngredients[callSite.actualCallee]!!
                                    if (genome[gene]) {
                                        scoreIngredients.irSize += calleeIngredients.irSize - 1
                                        //scoreIngredients.needsOwnFrame = scoreIngredients.needsOwnFrame || calleeIngredients.needsOwnFrame
                                    }
                                    // The callee's frame moves to the caller's.
                                    val discount = if (genome[gene]/* && calleeIngredients.needsOwnFrame*/) 1 else 0
                                    val discountedComplexity = calleeIngredients.framesTraversalComplexity - discount
                                    framesTraversalComplexity += max(0.0f, discountedComplexity) * pow(loopsPenalty * 1.0f, depth)
                                }
                        //if (scoreIngredients.needsOwnFrame)
                        framesTraversalComplexity += 1
                        scoreIngredients.framesTraversalComplexity = framesTraversalComplexity
                    }
                    ++counter
                } while (hasRecursion && counter < loopsPenalty)
            }

            val framesTraversalComplexity = rootSet.sumOf { functionsScoreIngredients[it]!!.framesTraversalComplexity.toDouble() }
            val irSwellingFactor = callGraph.directEdges.keys.sumByLong { functionsScoreIngredients[it]!!.irSize.toLong() } * 1.0 / totalIrSize
            return (framesTraversalComplexity * kotlin.math.sqrt(irSwellingFactor)).toFloat()
        }


    }

    fun run4(): Map<IrFunction, Set<IrFunction>> {
        // Compute call sites ids.
        var id = 0
        val callSiteIds = mutableMapOf<CallGraphNode.CallSite, Int>()
        for (callGraphNode in callGraph.directEdges.values) {
            callGraphNode.callSites
                    .filter { !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee) }
                    .forEach { callSiteIds[it] = id++ }
        }

        // Find backward edges.
        val forbiddenToInline = BitSet()
        val computationStates = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, ComputationState>()
        val stack = rootSet.toMutableList()
        for (root in stack)
            computationStates[root] = ComputationState.NEW
        while (stack.isNotEmpty()) {
            val functionSymbol = stack.peek()!!
            val state = computationStates[functionSymbol]!!
            val callSites = callGraph.directEdges[functionSymbol]!!.callSites.filter {
                !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee)
            }
            when (state) {
                ComputationState.NEW -> {
                    computationStates[functionSymbol] = ComputationState.PENDING
                    for (callSite in callSites) {
                        val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
                        if (computationStates[calleeSymbol] == null || computationStates[calleeSymbol] == ComputationState.NEW) {
                            computationStates[calleeSymbol] = ComputationState.NEW
                            stack.push(calleeSymbol)
                        }
                    }
                }

                ComputationState.PENDING -> {
                    stack.pop()
                    computationStates[functionSymbol] = ComputationState.DONE
                    for (callSite in callSites) {
                        val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
                        if (computationStates[calleeSymbol] != ComputationState.DONE)
                            forbiddenToInline.set(callSiteIds[callSite]!!)
//
//                        val calleeIrFunction = calleeSymbol.irFunction ?: continue
//                        if ((calleeIrFunction.origin == DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION && !options.inlineBoxUnbox)
//                                || calleeIrFunction.hasAnnotation(noInline)
//                                || (calleeIrFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.hasAnnotation(noInline) == true
//                                || (calleeIrFunction as? IrSimpleFunction)?.overrides(invokeSuspendFunction.owner) == true // TODO: Is it worth trying to support?
//                                || (calleeIrFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.let {
//                                    it.parentClassOrNull?.isSingleFieldValueClass == true || it.backingField != null
//                                } == true
//                                || (calleeIrFunction as? IrConstructor)?.constructedClass?.let { it.isArray || it.symbol == string } == true
//                                || (calleeIrFunction as? IrConstructor)?.constructedClass?.getAllSuperclasses()?.contains(throwable.owner) == true
//                        ) {
//                            forbiddenToInline.set(callSiteInfos[callSite]!!)
//                        }
                    }
                }

                ComputationState.DONE -> {
                    stack.pop()
                }
            }
        }

        val functionSizes = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, Int>()
        callGraph.directEdges.keys.forEach { functionSymbol ->
            val function = moduleDFG.functions[functionSymbol]!!
            functionSizes[functionSymbol] = function.body.allScopes.sumOf { it.nodes.size }
        }

        val handledFunctions = mutableSetOf<DataFlowIR.FunctionSymbol.Declared>()
        callGraph.directEdges.keys.forEach { functionSymbol ->
            val irFunction = functionSymbol.irFunction ?: return@forEach
            val function = moduleDFG.functions[functionSymbol]!!
            var isALoop = false
            function.body.forEachNonScopeNode { node ->
                if (node is DataFlowIR.Node.Call && node.callee == functionSymbol)
                    isALoop = true
            }
            if (isALoop
                    || (irFunction.origin == DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION && !options.inlineBoxUnbox)
                    || irFunction.hasAnnotation(noInline)
                    || (irFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.hasAnnotation(noInline) == true
                    || (irFunction as? IrSimpleFunction)?.overrides(invokeSuspendFunction.owner) == true // TODO: Is it worth trying to support?
                    || (irFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.let {
                        it.parentClassOrNull?.isSingleFieldValueClass == true || it.backingField != null
                    } == true
                    || (irFunction as? IrConstructor)?.constructedClass?.let { it.isArray || it.symbol == string } == true
                    || (irFunction as? IrConstructor)?.constructedClass?.getAllSuperclasses()?.contains(throwable.owner) == true
            ) {
                handledFunctions.add(functionSymbol)
            }
        }

        val inliningPolicy = BitSet()
        val threshold = 33
        while (true) {
            val smallestSize = functionSizes
                    .filter { it.key !in handledFunctions }
                    .minOf { it.value }
            if (smallestSize > threshold) break

            val justInlinedFunctions = mutableSetOf<DataFlowIR.FunctionSymbol.Declared>()
            callGraph.directEdges.forEach { (functionSymbol, callGraphNode) ->
                val callSites = callGraphNode.callSites.filter {
                    !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee)
                }
                var functionSize = functionSizes[functionSymbol]!!
                if (functionSize == smallestSize)
                    justInlinedFunctions.add(functionSymbol)
                for (callSite in callSites) {
                    val callSiteId = callSiteIds[callSite]!!
                    val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
                    val calleeSize = functionSizes[calleeSymbol]!!
                    if (calleeSize == smallestSize && calleeSymbol !in handledFunctions && !forbiddenToInline[callSiteId]) {
                        inliningPolicy.set(callSiteId)
                        functionSize = functionSize - 1 + calleeSize
                    }
                }
                functionSizes[functionSymbol] = functionSize
            }

            handledFunctions.addAll(justInlinedFunctions)
        }

        val result = mutableMapOf<IrFunction, Set<IrFunction>>()
        for ((functionSymbol, callGraphNode) in callGraph.directEdges) {
            val irFunction = functionSymbol.irFunction ?: continue
            result[irFunction] = callGraphNode.callSites
                    .filter {
                        !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee)
                                && inliningPolicy[callSiteIds[it]!!]
                    }
                    .map { it.actualCallee.irFunction!! }
                    .toSet()
        }
        return result
    }

    fun run() {
        val allFunctionsToInline = run4()
        val computationStates = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, ComputationState>()
        val stack = rootSet.toMutableList()
        for (root in stack)
            computationStates[root] = ComputationState.NEW

//        var count = 0

        while (stack.isNotEmpty()) {
            val functionSymbol = stack.peek()!!
            val function = moduleDFG.functions[functionSymbol]!!
            val state = computationStates[functionSymbol]!!
            val callSites = callGraph.directEdges[functionSymbol]!!.callSites.filter {
                !it.isVirtual && callGraph.directEdges.containsKey(it.actualCallee)
            }
            when (state) {
                ComputationState.NEW -> {
                    computationStates[functionSymbol] = ComputationState.PENDING
                    for (callSite in callSites) {
                        val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
                        if (computationStates[calleeSymbol] == null || computationStates[calleeSymbol] == ComputationState.NEW) {
                            computationStates[calleeSymbol] = ComputationState.NEW
                            stack.push(calleeSymbol)
                        }
//                        when (computationStates[calleeSymbol]) {
//                            null -> {
//                                computationStates[calleeSymbol] = ComputationState.NEW
//                                stack.push(calleeSymbol)
//                            }
//                            ComputationState.NEW, ComputationState.DONE -> Unit
//                            ComputationState.PENDING -> TODO()
//                        }
                    }
                }

                ComputationState.PENDING -> {
                    stack.pop()
                    computationStates[functionSymbol] = ComputationState.DONE

                    val irFunction = functionSymbol.irFunction ?: continue
                    val irBody = irFunction.body ?: continue
////                    if (irFunction.name.asString() == "foo")
//                    println("Handling ${irFunction.render()}")
                    val functionsToInline = mutableSetOf<IrFunction>()
                    val devirtualizedCallSitesFromFunctionsToInline = mutableMapOf<IrCall, DevirtualizationAnalysis.DevirtualizedCallSite>()
                    for (callSite in callSites) {
                        val calleeSymbol = callSite.actualCallee as DataFlowIR.FunctionSymbol.Declared
////                        if (irFunction.name.asString() == "foo") {
//                        println("    call to ${calleeSymbol.irFunction?.render()}")
////                            println("        ${computationStates[calleeSymbol]} ${calleeSymbol.irFunction?.render()}")
////                        }
                        if (computationStates[calleeSymbol] != ComputationState.DONE) continue
                        val calleeIrFunction = calleeSymbol.irFunction ?: continue
                        val callee = moduleDFG.functions[calleeSymbol]!!

//                        var isALoop = false
//                        callee.body.forEachNonScopeNode { node ->
//                            if (node is DataFlowIR.Node.Call && node.callee == calleeSymbol)
//                                isALoop = true
//                        }
//
//                        val calleeSize = callee.body.allScopes.sumOf { it.nodes.size }
////                        //if (irFunction.name.asString() == "foo")
////                        println("        $isALoop $calleeSize")
//                        val threshold = if (calleeIrFunction is IrSimpleFunction) 33 else 33
//                        val shouldInline = !isALoop && calleeSize <= threshold // TODO: To a function. Also use relative criterion along with the absolute one.
//                                && (calleeIrFunction.origin != DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION || options.inlineBoxUnbox)
//                                && !calleeIrFunction.hasAnnotation(noInline)
//                                && (calleeIrFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.hasAnnotation(noInline) != true
//                                && (calleeIrFunction as? IrSimpleFunction)?.overrides(invokeSuspendFunction.owner) != true // TODO: Is it worth trying to support?
//                                && (calleeIrFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.let {
//                            it.parentClassOrNull?.isSingleFieldValueClass == true && it.backingField != null
//                        } != true
//                                && (calleeIrFunction as? IrConstructor)?.constructedClass?.let { it.isArray || it.symbol == string } != true
//                                && (calleeIrFunction as? IrConstructor)?.constructedClass?.getAllSuperclasses()?.contains(throwable.owner) != true
//                        /*&& irFunction.fileOrNull?.path?.endsWith("tt.kt") == true*/
                        val shouldInline = calleeIrFunction in allFunctionsToInline[irFunction]!!// && !isALoop
                        if (shouldInline) {
                            if (functionsToInline.add(calleeIrFunction)) {
//                                if (calleeIrFunction is IrConstructor && calleeIrFunction.constructedClass.name.asString() == "ConcurrentModificationException") {
//                                    println("ZZZ: ${calleeIrFunction.render()}")
//                                    calleeIrFunction.constructedClass.getAllSuperclasses()
//                                }
                                callee.body.forEachVirtualCall { node ->
                                    val devirtualizedCallSite = devirtualizedCallSites[node]
                                    if (devirtualizedCallSite != null)
                                        devirtualizedCallSitesFromFunctionsToInline[node.irCallSite!!] = devirtualizedCallSite
                                }
                            }
                        }
                    }

                    updateMemoryUsage()

                    if (functionsToInline.isEmpty()) {
//                        println("Nothing to inline to ${irFunction.render()}")
//                        function.body.forEachVirtualCall { node ->
//                            val devirtualizedCallSite = devirtualizedCallSites[node]
//                            if (devirtualizedCallSite != null)
//                                rebuiltDevirtualizedCallSites[node] = devirtualizedCallSite
//                        }
                    } else {

                        /*
                        100 +
                        1000 -
                        500 +
                        750 -
                        625 +
                        690 +
                        720 +
                        735 -
                        727 -
                        723 -
                        721 -

                        1000 -
                        500 +
                        750 +
                        875 -
                        810 -
                        780 +
                        795 -
                        787 -
                        783 +
                        785 -
                        784 +

                        1000 -
                        500 +
                        750 +
                        875 -
                        810 -
                        780 -
                        765 -
                        757 +
                        761 -
                        759 +
                        760 -

                        1000 -
                        500 +
                        750 +
                        875 +
                        930 +
                        965 -
                        945 -
                        938 +
                        942 -
                        939 +
                        940 +
                        941 -
                         */
//                        ++count
////                        if (count == 941) {
////                            println("ZZZ: ${irFunction.fileOrNull?.path} ${irFunction.render()}")
////                            //functionsToInline.forEach { println("    ${it.render()}") }
////                            functionsToInline.forEach { println("    ${it.dump()}") }
////                            //println(irFunction.dump())
////                            println("BEFORE: ${irFunction.dump()}")
////                        }
//                        if (count > 941)
//                            continue

//                        if (irFunction is IrConstructor && irFunction.constructedClass.name.asString() == "ArrayList" && irFunction.valueParameters.size == 6)
//                            continue

//                        if (irFunction is IrConstructor && irFunction.constructedClass.name.asString() == "ArrayList" && irFunction.valueParameters.size == 1)
//                            println("ZZZ: ${irFunction.render()}")
////                            continue

//                        println("Preparing to inline to ${irFunction.render()}")
////                        functionsToInline.forEach { println("    ${it.dump()}") }
//                        functionsToInline.forEach { println("    ${it.render()}") }
//                        println("BEFORE: ${irFunction.dump()}")
                        val inliner = FunctionInlining(
                                context,
                                inlineFunctionResolver = object : InlineFunctionResolver() {
                                    override fun shouldExcludeFunctionFromInlining(symbol: IrFunctionSymbol) =
                                            symbol.owner !in functionsToInline
                                                    || (symbol.owner as? IrConstructor)?.constructedClass?.name?.asString() == "IllegalArgumentException"

                                },
                                devirtualizedCallSitesFromFunctionsToInline,
                        )
                        val devirtualizedCallSitesFromInlinedFunctions = inliner.lower(irBody, irFunction)

////                        if (count == 941)
//                            println("AFTER: ${irFunction.dump()}")

                        LivenessAnalysis.run(irBody) { it is IrSuspensionPoint }
                                .forEach { (irElement, liveVariables) ->
                                    generationState.liveVariablesAtSuspensionPoints[irElement as IrSuspensionPoint] = liveVariables
                                }

                        val devirtualizedCallSitesFromFunction = mutableMapOf<IrCall, DevirtualizationAnalysis.DevirtualizedCallSite>()
                        function.body.forEachVirtualCall { node ->
                            val devirtualizedCallSite = devirtualizedCallSites[node]
                            if (devirtualizedCallSite != null) {
                                devirtualizedCallSitesFromFunction[node.irCallSite!!] = devirtualizedCallSite
                                devirtualizedCallSites.remove(node)
                            }
                        }

                        val rebuiltFunction = FunctionDFGBuilder(generationState, moduleDFG.symbolTable).build(irFunction, irBody)
                        moduleDFG.functions[functionSymbol] = rebuiltFunction
                        rebuiltFunction.body.forEachVirtualCall { node ->
                            val irCallSite = node.irCallSite!!
                            val devirtualizedCallSite = devirtualizedCallSitesFromInlinedFunctions[irCallSite]
                                    ?: devirtualizedCallSitesFromFunction[irCallSite]
                            if (devirtualizedCallSite != null)
                                devirtualizedCallSites[node] = devirtualizedCallSite
                        }

                        updateMemoryUsage()

                        /*
                        +                    val body = when (declaration) {
                        +                        is IrFunction -> {
                        +                            context.logMultiple {
                        +                                +"Analysing function ${declaration.render()}"
                        +                                +"IR: ${declaration.dump()}"
                        +                            }
                        +                            declaration.body!!.also { body ->
                        +                                LivenessAnalysis.run(body) { it is IrSuspensionPoint }
                        +                                        .forEach { (irElement, liveVariables) ->
                        +                                            generationState.liveVariablesAtSuspensionPoints[irElement as IrSuspensionPoint] = liveVariables
                        +                                        }
                        +                            }
                        +                        }
                        +
                        +                        is IrField -> {
                        +                            context.logMultiple {
                        +                                +"Analysing global field ${declaration.render()}"
                        +                                +"IR: ${declaration.dump()}"
                        +                            }
                        +                            val initializer = declaration.initializer!!
                        +                            IrSetFieldImpl(initializer.startOffset, initializer.endOffset, declaration.symbol, null,
                        +                                    initializer.expression, context.irBuiltIns.unitType)
                        +                        }
                        +
                        +                        else -> error("Unexpected declaration: ${declaration.render()}")
                        +                    }
                        +
                        +
                        +//                    println("AFTER polishing: ${declaration.dump()}")
                        +                    val function = FunctionDFGBuilder(generationState, input.moduleDFG.symbolTable).build(declaration, body)
                        +                    input.moduleDFG.functions[function.symbol] = function
                         */
                    }
                }

                ComputationState.DONE -> {
                    stack.pop()
                }
            }
        }

//        println("During BackendInlinerPhase: $maxMemoryUsage")

    }

    private enum class ComputationState {
        NEW,
        PENDING,
        DONE
    }

}

internal abstract class InlineFunctionResolver {
    fun getFunctionDeclaration(symbol: IrFunctionSymbol): IrFunction? {
        if (shouldExcludeFunctionFromInlining(symbol)) return null

        val owner = symbol.owner
        return (owner as? IrSimpleFunction)?.resolveFakeOverride() ?: owner
    }

    protected abstract fun shouldExcludeFunctionFromInlining(symbol: IrFunctionSymbol): Boolean
}

internal class FunctionInlining(
        private val context: Context,
        private val inlineFunctionResolver: InlineFunctionResolver,
        private val devirtualizedCallSites: Map<IrCall, DevirtualizationAnalysis.DevirtualizedCallSite>,
) : IrElementTransformerVoidWithContext() {
    private val unitType = context.irBuiltIns.unitType
    private val createUninitializedInstance = context.ir.symbols.createUninitializedInstance
    private val initInstance = context.ir.symbols.initInstance

    private var containerScope: ScopeWithIr? = null
    private val elementsWithLocationToPatch = hashSetOf<IrGetValue>()
    private val copiedDevirtualizedCallSites = mutableMapOf<IrCall, DevirtualizationAnalysis.DevirtualizedCallSite>()

    fun lower(irBody: IrBody, container: IrDeclaration): Map<IrCall, DevirtualizationAnalysis.DevirtualizedCallSite> {
        // TODO container: IrSymbolDeclaration
        containerScope = createScope(container as IrSymbolOwner)
        irBody.accept(this, null)
        containerScope = null

        irBody.patchDeclarationParents(container as? IrDeclarationParent ?: container.parent)

        return copiedDevirtualizedCallSites
    }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression) = when (expression.symbol) {
        initInstance -> {
            val instance = expression.getValueArgument(0)!!
            val constructorCall = expression.getValueArgument(1) as IrConstructorCall
            instance.transformChildrenVoid()
            constructorCall.transformChildrenVoid()
            tryInline(constructorCall, instance)
        }
        else -> {
            expression.transformChildrenVoid()
            tryInline(expression, null)
        }
    } ?: expression

    private val IrConstructor.delegatingConstructorCallIsNoop: Boolean
        get() = constructedClass.isExternalObjCClass() || constructedClass.isAny()

    private fun tryInline(callSite: IrFunctionAccessExpression, instance: IrExpression?): IrExpression? {
        val calleeSymbol = callSite.symbol
        val actualCallee = inlineFunctionResolver.getFunctionDeclaration(calleeSymbol)
        if (actualCallee?.body == null || callSite.isVirtualCall) {
            return null
        }

        val parent = allScopes.map { it.irElement }.filterIsInstance<IrDeclarationParent>().lastOrNull()
                ?: allScopes.map { it.irElement }.filterIsInstance<IrDeclaration>().lastOrNull()?.parent
                ?: containerScope?.irElement as? IrDeclarationParent
                ?: (containerScope?.irElement as? IrDeclaration)?.parent

        val inliner = Inliner(callSite, instance, actualCallee, currentScope ?: containerScope!!, parent, context)
        return inliner.inline()
    }

    private inner class Inliner(
            val callSite: IrFunctionAccessExpression,
            val givenInstance: IrExpression?,
            val callee: IrFunction,
            val currentScope: ScopeWithIr,
            val parent: IrDeclarationParent?,
            val context: CommonBackendContext
    ) {
        val copyIrElement = run {
//            val typeParameters =
//                    if (callee is IrConstructor)
//                        callee.parentAsClass.typeParameters
//                    else callee.typeParameters
//            val typeArguments =
//                    (0 until callSite.typeArgumentsCount).associate {
//                        typeParameters[it].symbol to callSite.getTypeArgument(it)
//                    }
            DeepCopyIrTreeWithSymbolsForInliner(/*typeArguments*/null, parent, devirtualizedCallSites, copiedDevirtualizedCallSites)
        }

        val substituteMap = mutableMapOf<IrValueParameter, IrExpression>()

        fun inline() = inlineFunction(callSite, callee, callee/*.originalFunction*/)

        private fun <E : IrElement> E.copy(): E {
            @Suppress("UNCHECKED_CAST")
            return copyIrElement.copy(this) as E
        }

        private fun inlineFunction(
                callSite: IrFunctionAccessExpression,
                callee: IrFunction,
                originalInlinedElement: IrElement,
        ): IrExpression {
            val copiedCallee = callee.copy().apply {
                parent = callee.parent
            }
//            println("AFTER copying: ${copiedCallee.dump()}")

            if (callee is IrConstructor) {
                val thisReceiver = callee.constructedClass.thisReceiver!!
                copiedCallee.transformChildrenVoid(object : IrElementTransformerVoid() {
                    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                        if (expression.symbol.owner.delegatingConstructorCallIsNoop)
                            return IrCompositeImpl(expression.startOffset, expression.endOffset, unitType)

                        val constructorCall = IrConstructorCallImpl(
                                expression.startOffset, expression.endOffset,
                                expression.type,
                                expression.symbol,
                                typeArgumentsCount = expression.typeArgumentsCount,
                                constructorTypeArgumentsCount = 0,
                                valueArgumentsCount = expression.valueArgumentsCount,
                                origin = expression.origin,
                        ).apply {
                            dispatchReceiver = expression.dispatchReceiver
                            (0..<expression.valueArgumentsCount).forEach {
                                putValueArgument(it, expression.getValueArgument(it))
                            }
                            (0..<expression.typeArgumentsCount).forEach {
                                putTypeArgument(it, expression.getTypeArgument(it))
                            }
                        }
                        return IrCallImpl(
                                expression.startOffset, expression.endOffset,
                                unitType,
                                initInstance,
                                typeArgumentsCount = 0,
                                valueArgumentsCount = 2,
                        ).apply {
                            putValueArgument(0, IrGetValueImpl(expression.startOffset, expression.endOffset, thisReceiver.symbol))
                            putValueArgument(1, constructorCall)
                        }
                    }
                })
            }

            val instance: IrValueDeclaration? = givenInstance?.let {
                currentScope.scope.createTemporaryVariable(it, nameHint = "\$inst")
            } ?: when (callSite) {
                is IrDelegatingConstructorCall -> (currentScope.irElement as IrConstructor).constructedClass.thisReceiver!!
                is IrConstructorCall -> {
                    val constructedClassType = (callee as IrConstructor).constructedClassType
                    currentScope.scope.createTemporaryVariable(
                            IrCallImpl(
                                    callSite.startOffset, callSite.endOffset,
                                    constructedClassType,
                                    createUninitializedInstance,
                                    typeArgumentsCount = 1,
                                    valueArgumentsCount = 0,
                            ).apply { putTypeArgument(0, constructedClassType) },
                            nameHint = "\$inst"
                    )
                }
                else -> null
            }

            val evaluationStatements = evaluateArguments(callSite, copiedCallee, instance)
            val statements = (copiedCallee.body as? IrBlockBody)?.statements
                    ?: error("Body not found for function ${callee.render()}")

            val irReturnableBlockSymbol = IrReturnableBlockSymbolImpl()
            val endOffset = statements.lastOrNull()?.endOffset ?: callee.endOffset
            /* creates irBuilder appending to the end of the given returnable block: thus why we initialize
             * irBuilder with (..., endOffset, endOffset).
             */
            val irBuilder = context.createIrBuilder(irReturnableBlockSymbol, endOffset, endOffset)

            val transformer = ParameterSubstitutor()
            val newStatements = statements.map { it.transform(transformer, data = null) as IrStatement }

            val returnType = if (callee is IrConstructor) unitType else callee.returnType.erasure()

            val inlinedBlock = IrInlinedFunctionBlockImpl(
                    startOffset = callSite.startOffset,
                    endOffset = callSite.endOffset,
                    type = returnType,
                    inlineCall = callSite,
                    inlinedElement = originalInlinedElement,
                    origin = null,
                    statements = evaluationStatements + newStatements
            )

            // Note: here we wrap `IrInlinedFunctionBlock` inside `IrReturnableBlock` because such way it is easier to
            // control special composite blocks that are inside `IrInlinedFunctionBlock`
            val returnableBlock = IrReturnableBlockImpl(
                    startOffset = callSite.startOffset,
                    endOffset = callSite.endOffset,
                    type = returnType,
                    symbol = irReturnableBlockSymbol,
                    origin = null,
                    statements = listOf(inlinedBlock),
            ).apply {
                transformChildrenVoid(object : IrElementTransformerVoid() {
                    override fun visitReturn(expression: IrReturn): IrExpression {
                        expression.transformChildrenVoid(this)

                        if (expression.returnTargetSymbol == copiedCallee.symbol)
                            return irBuilder.at(expression).irReturn(expression.value)

                        return expression
                    }
                })
                patchDeclarationParents(parent) // TODO: Why it is not enough to just run SetDeclarationsParentVisitor?
            }

            return if (instance !is IrVariable)
                returnableBlock
            else if (givenInstance != null) {
                IrBlockImpl(
                        callSite.startOffset, callSite.endOffset,
                        unitType,
                        origin = null,
                        statements = listOf(
                                instance,
                                returnableBlock,
                        )
                )
            } else {
                IrBlockImpl(
                        callSite.startOffset, callSite.endOffset,
                        (callee as IrConstructor).constructedClassType,
                        origin = null,
                        statements = listOf(
                                instance,
                                returnableBlock,
                                IrGetValueImpl(callSite.startOffset, callSite.endOffset, instance.symbol)
                        )
                )
            }
        }

        private inner class ParameterSubstitutor : IrElementTransformerVoid() {
            override fun visitGetValue(expression: IrGetValue): IrExpression {
                val newExpression = super.visitGetValue(expression) as IrGetValue
                val argument = substituteMap[newExpression.symbol.owner] ?: return newExpression

                argument.transformChildrenVoid(this) // Default argument can contain subjects for substitution.

                return if (argument is IrGetValue && argument in elementsWithLocationToPatch)
                    argument.copyWithOffsets(newExpression.startOffset, newExpression.endOffset)
                else
                    argument.copy()
            }

            override fun visitElement(element: IrElement) = element.accept(this, null)
        }

        private inner class ParameterToArgument(
                val parameter: IrValueParameter,
                val argumentExpression: IrExpression,
                val isDefaultArg: Boolean = false
        ) {
            val isImmutableVariableLoad: Boolean
                get() = argumentExpression.let { argument ->
                    argument is IrGetValue && !argument.symbol.owner.let { it is IrVariable && it.isVar }
                }
        }

        // callee might be a copied version of callsite.symbol.owner
        private fun buildParameterToArgument(callSite: IrFunctionAccessExpression, callee: IrFunction, instance: IrValueDeclaration?): List<ParameterToArgument> {
            val parameterToArgument = mutableListOf<ParameterToArgument>()

            if (instance != null)
                parameterToArgument += ParameterToArgument(
                        parameter = (callee as IrConstructor).constructedClass.thisReceiver!!,
                        argumentExpression = IrGetValueImpl(
                                callSite.startOffset, callSite.endOffset,
                                instance.symbol,
                        )
                )

            if (callSite.dispatchReceiver != null && callee.dispatchReceiverParameter != null)
                parameterToArgument += ParameterToArgument(
                        parameter = callee.dispatchReceiverParameter!!,
                        argumentExpression = callSite.dispatchReceiver!!
                )

            val valueArguments =
                    callSite.symbol.owner.valueParameters.map { callSite.getValueArgument(it.index) }.toMutableList()

            if (callee.extensionReceiverParameter != null) {
                parameterToArgument += ParameterToArgument(
                        parameter = callee.extensionReceiverParameter!!,
                        argumentExpression = if (callSite.extensionReceiver != null) {
                            callSite.extensionReceiver!!
                        } else {
                            // Special case: lambda with receiver is called as usual lambda:
                            valueArguments.removeAt(0)!!
                        }
                )
            } else if (callSite.extensionReceiver != null) {
                // Special case: usual lambda is called as lambda with receiver:
                valueArguments.add(0, callSite.extensionReceiver!!)
            }

            val parametersWithDefaultToArgument = mutableListOf<ParameterToArgument>()
            for (parameter in callee.valueParameters) {
                val argument = valueArguments[parameter.index]
                when {
                    argument != null -> {
                        parameterToArgument += ParameterToArgument(
                                parameter = parameter,
                                argumentExpression = argument
                        )
                    }

                    // After ExpectDeclarationsRemoving pass default values from expect declarations
                    // are represented correctly in IR.
                    parameter.defaultValue != null -> {  // There is no argument - try default value.
                        parametersWithDefaultToArgument += ParameterToArgument(
                                parameter = parameter,
                                argumentExpression = parameter.defaultValue!!.expression,
                                isDefaultArg = true
                        )
                    }

                    parameter.varargElementType != null -> {
                        val emptyArray = IrVarargImpl(
                                startOffset = callSite.startOffset,
                                endOffset = callSite.endOffset,
                                type = parameter.type,
                                varargElementType = parameter.varargElementType!!
                        )
                        parameterToArgument += ParameterToArgument(
                                parameter = parameter,
                                argumentExpression = emptyArray
                        )
                    }

                    else -> {
                        val message = "Incomplete expression: call to ${callee.render()} " +
                                "has no argument at index ${parameter.index}"
                        throw Error(message)
                    }
                }
            }
            // All arguments except default are evaluated at callsite,
            // but default arguments are evaluated inside callee.
            return parameterToArgument + parametersWithDefaultToArgument
        }

        private fun evaluateArguments(callSite: IrFunctionAccessExpression, callee: IrFunction, instance: IrValueDeclaration?): List<IrStatement> {
            val arguments = buildParameterToArgument(callSite, callee, instance)
            val evaluationStatements = mutableListOf<IrVariable>()
            val evaluationStatementsFromDefault = mutableListOf<IrVariable>()
            val substitutor = ParameterSubstitutor()
            arguments.forEach { argument ->
                val parameter = argument.parameter

                // Arguments may reference the previous ones - substitute them.
                val variableInitializer = argument.argumentExpression.transform(substitutor, data = null)
                val shouldCreateTemporaryVariable = argument.shouldBeSubstitutedViaTemporaryVariable()

                if (shouldCreateTemporaryVariable) {
                    val newVariable = createTemporaryVariable(parameter, variableInitializer, argument.isDefaultArg, callee)
                    if (argument.isDefaultArg) evaluationStatementsFromDefault.add(newVariable) else evaluationStatements.add(newVariable)
                    substituteMap[parameter] = irGetValueWithoutLocation(newVariable.symbol)
                    return@forEach
                }

                substituteMap[parameter] = if (variableInitializer is IrGetValue) {
                    irGetValueWithoutLocation(variableInitializer.symbol)
                } else {
                    variableInitializer
                }
            }

            // Next two composite blocks are used just as containers for two types of variables.
            // First one store temp variables that represent non default arguments of inline call and second one store defaults.
            // This is needed because these two groups of variables need slightly different processing on (JVM) backend.
            val blockForNewStatements = IrCompositeImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.unitType,
                    LoweredStatementOrigins.INLINED_FUNCTION_ARGUMENTS, statements = evaluationStatements
            )

            val blockForNewStatementsFromDefault = IrCompositeImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.unitType,
                    LoweredStatementOrigins.INLINED_FUNCTION_DEFAULT_ARGUMENTS, statements = evaluationStatementsFromDefault
            )

            return listOfNotNull(
                    blockForNewStatements.takeIf { evaluationStatements.isNotEmpty() },
                    blockForNewStatementsFromDefault.takeIf { evaluationStatementsFromDefault.isNotEmpty() }
            )
        }

        private fun ParameterToArgument.shouldBeSubstitutedViaTemporaryVariable(): Boolean =
                !(isImmutableVariableLoad && parameter.index >= 0) && !argumentExpression.isPure(false, context = context)

        private fun createTemporaryVariable(
                parameter: IrValueParameter,
                variableInitializer: IrExpression,
                isDefaultArg: Boolean,
                callee: IrFunction
        ): IrVariable {
            val variable = currentScope.scope.createTemporaryVariable(
                    irExpression = IrBlockImpl(
                            if (isDefaultArg) variableInitializer.startOffset else UNDEFINED_OFFSET,
                            if (isDefaultArg) variableInitializer.endOffset else UNDEFINED_OFFSET,
                            // If original type of parameter is T, then `parameter.type` is T after substitution or erasure,
                            // depending on whether T reified or not.
                            parameter.type
                    ).apply {
                        statements.add(variableInitializer)
                    },
                    nameHint = callee.symbol.owner.name.asStringStripSpecialMarkers(),
                    isMutable = false,
                    origin = if (parameter == callee.extensionReceiverParameter) {
                        IrDeclarationOrigin.IR_TEMPORARY_VARIABLE_FOR_INLINED_EXTENSION_RECEIVER
                    } else {
                        IrDeclarationOrigin.IR_TEMPORARY_VARIABLE_FOR_INLINED_PARAMETER
                    }
            )

            variable.name = Name.identifier(parameter.name.asStringStripSpecialMarkers())

            return variable
        }
    }

    private fun irGetValueWithoutLocation(
            symbol: IrValueSymbol,
            origin: IrStatementOrigin? = null,
    ): IrGetValue {
        return IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, symbol, origin).also {
            elementsWithLocationToPatch += it
        }
    }
}

internal class DeepCopyIrTreeWithSymbolsForInliner(
        val typeArguments: Map<IrTypeParameterSymbol, IrType?>?,
        val parent: IrDeclarationParent?,
        val devirtualizedCallSites: Map<IrCall, DevirtualizationAnalysis.DevirtualizedCallSite>,
        val copiedDevirtualizedCallSites: MutableMap<IrCall, DevirtualizationAnalysis.DevirtualizedCallSite>,
) {
    fun copy(irElement: IrElement): IrElement {
        // Create new symbols.
        irElement.acceptVoid(symbolRemapper)

        // Make symbol remapper aware of the callsite's type arguments.
        symbolRemapper.typeArguments = typeArguments

        // Copy IR.
        val result = irElement.transform(copier, data = null)

        result.patchDeclarationParents(parent)
        return result
    }

    private inner class InlinerTypeRemapper(
            val symbolRemapper: SymbolRemapper,
            val typeArguments: Map<IrTypeParameterSymbol, IrType?>?,
    ) : TypeRemapper {
        override fun enterScope(irTypeParametersContainer: IrTypeParametersContainer) {}

        override fun leaveScope() {}

        private fun remapTypeArguments(
                arguments: List<IrTypeArgument>,
                erasedParameters: MutableSet<IrTypeParameterSymbol>?,
        ) =
                arguments.memoryOptimizedMap { argument ->
                    (argument as? IrTypeProjection)?.let { proj ->
                        remapType(proj.type, erasedParameters)?.let { newType ->
                            makeTypeProjection(newType, proj.variance)
                        } ?: IrStarProjectionImpl
                    }
                            ?: argument
                }

        override fun remapType(type: IrType) =
                remapType(type, mutableSetOf()) ?: error("Cannot substitute type ${type.render()}")

        private fun remapType(type: IrType, erasedParameters: MutableSet<IrTypeParameterSymbol>?): IrType? {
            if (type !is IrSimpleType) return type

            val classifier = type.classifier
            val substitutedType = typeArguments?.get(classifier)

            // Erase non-reified type parameter if asked to.
            if (erasedParameters != null && substitutedType != null && (classifier as? IrTypeParameterSymbol)?.owner?.isReified == false) {
                if (classifier in erasedParameters) {
                    return null
                }

                erasedParameters.add(classifier)

                // Pick the (necessarily unique) non-interface upper bound if it exists.
                val superTypes = classifier.owner.superTypes
                val superClass = superTypes.firstOrNull {
                    it.classOrNull?.owner?.isInterface == false
                }

                val upperBound = superClass ?: superTypes.first()

                // TODO: Think about how to reduce complexity from k^N to N^k
                val erasedUpperBound = remapType(upperBound, erasedParameters)
                        ?: error("Cannot erase upperbound ${upperBound.render()}")

                erasedParameters.remove(classifier)

                return erasedUpperBound.mergeNullability(type)
            }

            if (substitutedType is IrDynamicType) return substitutedType

            if (substitutedType is IrSimpleType) {
                return substitutedType.mergeNullability(type)
            }

            return type.buildSimpleType {
                kotlinType = null
                this.classifier = symbolRemapper.getReferencedClassifier(classifier)
                arguments = remapTypeArguments(type.arguments, erasedParameters)
                annotations = type.annotations.memoryOptimizedMap { it.transform(copier, null) as IrConstructorCall }
            }
        }
    }

    private class SymbolRemapperImpl(descriptorsRemapper: DescriptorsRemapper) : DeepCopySymbolRemapper(descriptorsRemapper) {
        var typeArguments: Map<IrTypeParameterSymbol, IrType?>? = null
            set(value) {
                if (field != null) return
                field = value?.asSequence()?.associate {
                    (getReferencedClassifier(it.key) as IrTypeParameterSymbol) to it.value
                }
            }

        override fun getReferencedClassifier(symbol: IrClassifierSymbol): IrClassifierSymbol {
            val result = super.getReferencedClassifier(symbol)
            if (result !is IrTypeParameterSymbol)
                return result
            return typeArguments?.get(result)?.classifierOrNull ?: result
        }
    }

    private val symbolRemapper = SymbolRemapperImpl(NullDescriptorsRemapper)
    private val typeRemapper = InlinerTypeRemapper(symbolRemapper, typeArguments)
    private val copier = object : DeepCopyIrTreeWithSymbols(symbolRemapper, typeRemapper) {
        private fun IrType.erase() = typeRemapper.remapType(this)

        override fun visitCall(expression: IrCall) = super.visitCall(expression).also { copiedCall ->
            val devirtualizedCallSite = devirtualizedCallSites[expression]
            if (devirtualizedCallSite != null)
                copiedDevirtualizedCallSites[copiedCall] = devirtualizedCallSite
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall) =
                IrTypeOperatorCallImpl(
                        expression.startOffset, expression.endOffset,
                        expression.type.erase(),
                        expression.operator,
                        expression.typeOperand.erase(),
                        expression.argument.transform()
                ).copyAttributes(expression)
    }
}
