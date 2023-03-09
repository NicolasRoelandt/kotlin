/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.cli.jvm.compiler

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.lifetime.KtAlwaysAccessibleLifetimeTokenFactory
import org.jetbrains.kotlin.analysis.api.session.KtAnalysisSessionProvider
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.KtSourceModule
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.findFacadeClass
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.JvmGeneratorExtensions
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.reportError
import org.jetbrains.kotlin.cli.jvm.config.addJavaSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.CodegenFactory
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.backend.*
import org.jetbrains.kotlin.fir.backend.jvm.*
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.pipeline.*
import org.jetbrains.kotlin.fir.session.*
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.fir.types.arrayElementType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isArrayType
import org.jetbrains.kotlin.fir.types.isString
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.modules.Module
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.multiplatform.hmppModuleName
import org.jetbrains.kotlin.resolve.multiplatform.isCommonSource
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.addToStdlib.runUnless
import java.io.File

object FirKotlinToJvmBytecodeCompiler {
    fun compileModulesUsingFrontendIR(
        projectEnvironment: AbstractProjectEnvironment,
        projectConfiguration: CompilerConfiguration,
        messageCollector: MessageCollector,
        allSources: List<KtFile>,
        buildFile: File?,
        chunk: List<Module>
    ): Boolean {
        val performanceManager = projectConfiguration.get(CLIConfigurationKeys.PERF_MANAGER)

        val notSupportedPlugins = mutableListOf<String?>().apply {
            projectConfiguration.get(ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS)
                .collectIncompatiblePluginNamesTo(this, ComponentRegistrar::supportsK2)
            projectConfiguration.get(CompilerPluginRegistrar.COMPILER_PLUGIN_REGISTRARS)
                .collectIncompatiblePluginNamesTo(this, CompilerPluginRegistrar::supportsK2)
        }

        if (notSupportedPlugins.isNotEmpty()) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                """
                    |There are some plugins incompatible with language version 2.0:
                    |${notSupportedPlugins.joinToString(separator = "\n|") { "  $it" }}
                    |Please use language version 1.9 or below
                """.trimMargin()
            )
            return false
        }

        val outputs = ArrayList<Pair<FirResult, GenerationState>>(chunk.size)
        val targetIds = projectConfiguration.get(JVMConfigurationKeys.MODULES)?.map(::TargetId)
        val incrementalComponents = projectConfiguration.get(JVMConfigurationKeys.INCREMENTAL_COMPILATION_COMPONENTS)
        val isMultiModuleChunk = chunk.size > 1

        // TODO: run lowerings for all modules in the chunk, then run codegen for all modules.
        val project = (projectEnvironment as? VfsBasedProjectEnvironment)?.project
        for (module in chunk) {
            val moduleConfiguration = projectConfiguration.applyModuleProperties(module, buildFile)
            val context = CompilationContext(
                module,
                module.getSourceFiles(
                    allSources, (projectEnvironment as? VfsBasedProjectEnvironment)?.localFileSystem, isMultiModuleChunk, buildFile
                ),
                projectEnvironment,
                messageCollector,
                moduleConfiguration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME),
                moduleConfiguration,
                performanceManager,
                targetIds,
                incrementalComponents,
                extensionRegistrars = project?.let { FirExtensionRegistrar.getInstances(it) } ?: emptyList(),
                irGenerationExtensions = project?.let { IrGenerationExtension.getInstances(it) } ?: emptyList()
            )

            if (project != null) {
                val analysisResult = context.processAnalysisHandlerExtensions(project)
                if (analysisResult != null) {
                    return analysisResult.success
                }
            }

            val generationState = context.compileModule() ?: return false
            outputs += generationState
        }

        val mainClassFqName: FqName? = runIf(chunk.size == 1 && projectConfiguration.get(JVMConfigurationKeys.OUTPUT_JAR) != null) {
            findMainClass(outputs.single().first.outputs.last().fir)
        }

        return writeOutputsIfNeeded(
            project,
            projectConfiguration,
            chunk,
            outputs.map(Pair<FirResult, GenerationState>::second),
            mainClassFqName
        )
    }

    private fun <T : Any> List<T>?.collectIncompatiblePluginNamesTo(
        destination: MutableList<String?>,
        supportsK2: T.() -> Boolean
    ) {
        this?.filter { !it.supportsK2() && it::class.java.canonicalName != CLICompiler.SCRIPT_PLUGIN_REGISTRAR_NAME }
            ?.mapTo(destination) { it::class.qualifiedName }
    }

    private fun CompilationContext.compileModule(): Pair<FirResult, GenerationState>? {
        performanceManager?.notifyAnalysisStarted()
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        if (!checkKotlinPackageUsage(moduleConfiguration, allSources)) return null

        val renderDiagnosticNames = moduleConfiguration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)

        val diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter()
        val firResult = runFrontend(allSources, diagnosticsReporter).also {
            performanceManager?.notifyAnalysisFinished()
        }
        if (firResult == null) {
            FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(diagnosticsReporter, messageCollector, renderDiagnosticNames)
            return null
        }

        performanceManager?.notifyGenerationStarted()
        performanceManager?.notifyIRTranslationStarted()

        val fir2IrExtensions = JvmFir2IrExtensions(moduleConfiguration, JvmIrDeserializerImpl(), JvmIrMangler)
        val fir2IrResult = firResult.convertToIrAndActualizeForJvm(
            fir2IrExtensions,
            irGenerationExtensions,
            linkViaSignatures = moduleConfiguration.getBoolean(JVMConfigurationKeys.LINK_VIA_SIGNATURES)
        )

        performanceManager?.notifyIRTranslationFinished()

        val generationState = runBackend(
            allSources,
            fir2IrResult,
            fir2IrExtensions,
            diagnosticsReporter
        )

        FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(diagnosticsReporter, messageCollector, renderDiagnosticNames)

        performanceManager?.notifyIRGenerationFinished()
        performanceManager?.notifyGenerationFinished()
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        return firResult to generationState
    }

    private fun CompilationContext.runFrontend(ktFiles: List<KtFile>, diagnosticsReporter: BaseDiagnosticsCollector): FirResult? {
        val syntaxErrors = ktFiles.fold(false) { errorsFound, ktFile ->
            AnalyzerWithCompilerReport.reportSyntaxErrors(ktFile, messageCollector).isHasErrors or errorsFound
        }

        val sourceScope = (projectEnvironment as VfsBasedProjectEnvironment).getSearchScopeByPsiFiles(ktFiles) +
                projectEnvironment.getSearchScopeForProjectJavaSources()

        var librariesScope = projectEnvironment.getSearchScopeForProjectLibraries()

        val providerAndScopeForIncrementalCompilation = createContextForIncrementalCompilation(
            projectEnvironment,
            incrementalComponents,
            moduleConfiguration,
            targetIds,
            sourceScope
        )

        providerAndScopeForIncrementalCompilation?.precompiledBinariesFileScope?.let {
            librariesScope -= it
        }
        val rootModuleName = module.getModuleName()
        val libraryList = createLibraryListForJvm(rootModuleName, moduleConfiguration, module.getFriendPaths())
        val sessionsWithSources = prepareJvmSessions(
            ktFiles, moduleConfiguration, projectEnvironment, rootModuleName,
            extensionRegistrars, librariesScope, libraryList,
            isCommonSource = { it.isCommonSource == true },
            fileBelongsToModule = { file, moduleName -> file.hmppModuleName == moduleName },
            createProviderAndScopeForIncrementalCompilation = { providerAndScopeForIncrementalCompilation }
        )

        val outputs = sessionsWithSources.map { (session, sources) ->
            buildResolveAndCheckFir(session, sources, diagnosticsReporter)
        }

        return runUnless(syntaxErrors || diagnosticsReporter.hasErrors) { FirResult(outputs) }
    }

    private fun CompilationContext.runBackend(
        ktFiles: List<KtFile>,
        fir2IrResult: Fir2IrResult,
        extensions: JvmGeneratorExtensions,
        diagnosticsReporter: BaseDiagnosticsCollector
    ): GenerationState {
        val (moduleFragment, components) = fir2IrResult
        val dummyBindingContext = NoScopeRecordCliBindingTrace().bindingContext
        val codegenFactory = JvmIrCodegenFactory(
            moduleConfiguration,
            moduleConfiguration.get(CLIConfigurationKeys.PHASE_CONFIG),
        )

        val generationState = GenerationState.Builder(
            (projectEnvironment as VfsBasedProjectEnvironment).project, ClassBuilderFactories.BINARIES,
            moduleFragment.descriptor, dummyBindingContext, moduleConfiguration
        ).withModule(
            module
        ).onIndependentPartCompilationEnd(
            createOutputFilesFlushingCallbackIfPossible(moduleConfiguration)
        ).isIrBackend(
            true
        ).jvmBackendClassResolver(
            FirJvmBackendClassResolver(components)
        ).diagnosticReporter(
            diagnosticsReporter
        ).build()

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        performanceManager?.notifyIRLoweringStarted()
        generationState.beforeCompile()
        generationState.oldBEInitTrace(ktFiles)
        codegenFactory.generateModuleInFrontendIRMode(
            generationState, moduleFragment, components.symbolTable, components.irProviders,
            extensions, FirJvmBackendExtension(components), fir2IrResult.pluginContext
        ) {
            performanceManager?.notifyIRLoweringFinished()
            performanceManager?.notifyIRGenerationStarted()
        }
        CodegenFactory.doCheckCancelled(generationState)
        generationState.factory.done()

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        AnalyzerWithCompilerReport.reportDiagnostics(
            FilteredJvmDiagnostics(
                generationState.collectedExtraJvmDiagnostics,
                dummyBindingContext.diagnostics
            ),
            messageCollector,
            renderDiagnosticName
        )
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        return generationState
    }

    private class CompilationContext(
        val module: Module,
        val allSources: List<KtFile>,
        val projectEnvironment: AbstractProjectEnvironment,
        val messageCollector: MessageCollector,
        val renderDiagnosticName: Boolean,
        val moduleConfiguration: CompilerConfiguration,
        val performanceManager: CommonCompilerPerformanceManager?,
        val targetIds: List<TargetId>?,
        val incrementalComponents: IncrementalCompilationComponents?,
        val extensionRegistrars: List<FirExtensionRegistrar>,
        val irGenerationExtensions: Collection<IrGenerationExtension>
    )

    private fun CompilationContext.processAnalysisHandlerExtensions(project: Project): K2AnalysisResult? {
        val extensions = FirAnalysisHandlerExtension.getInstances(project)
        val extension = when (extensions.size) {
            0 -> return null
            1 -> extensions.single()
            else -> {
                val extensionNames = extensions.map { it::class.qualifiedName }
                messageCollector.reportError("It's allowed to register only one FirAnalysisHandlerExtension, but several are registered: $extensionNames")
                return null
            }
        }

        val configuration = moduleConfiguration

        while (true) {
            val result = try {
                processAnalysisHandlerExtensionOnce(project, configuration, extension)
            } catch (e: Exception) {
                K2AnalysisResult.InternalError(e)
            }
            when (result) {
                is K2AnalysisResult.RetryWithAdditionalRoots -> {
                    configuration.update {
                        addJavaSourceRoots(result.additionalJavaRoots)
                        addJvmClasspathRoots(result.additionalClassPathRoots)
                        addKotlinSourceRoots(result.additionalKotlinRoots.map { it.absolutePath })
                    }

                    val lookupTracker = configuration[CommonConfigurationKeys.LOOKUP_TRACKER]
                    lookupTracker?.clear()

                    // Clear all diagnostic messages
                    configuration[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY]?.clear()
                }
                else -> return result
            }
        }
    }

    private inline fun CompilerConfiguration.update(block: CompilerConfiguration.() -> Unit) {
        val oldReadOnlyValue = isReadOnly
        isReadOnly = false
        try {
            block()
        } finally {
            isReadOnly = oldReadOnlyValue
        }
    }

    @OptIn(KtAnalysisApiInternals::class)
    private fun processAnalysisHandlerExtensionOnce(
        project: Project,
        configuration: CompilerConfiguration,
        extension: FirAnalysisHandlerExtension
    ): K2AnalysisResult {
        val module: KtSourceModule

        val analysisSession = buildStandaloneAnalysisAPISession(projectDisposable = project) {
            buildKtModuleProviderByCompilerConfiguration(configuration) {
                module = it
            }
        }
        val ktAnalysisSession = KtAnalysisSessionProvider.getInstance(analysisSession.project)
            .getAnalysisSessionByUseSiteKtModule(module, KtAlwaysAccessibleLifetimeTokenFactory)
        val ktFiles = module.ktFiles

        val lightClasses = mutableListOf<Pair<KtLightClass, KtFile>>().apply {
            ktFiles.flatMapTo(this) { file ->
                file.children.filterIsInstance<KtClassOrObject>().mapNotNull {
                    it.toLightClass()?.let { it to file }
                }
            }
            ktFiles.mapNotNullTo(this) { ktFile -> ktFile.findFacadeClass()?.let { it to ktFile } }
        }.toMap()

        return extension.doAnalysis(ktAnalysisSession, lightClasses)
    }
}

fun findMainClass(fir: List<FirFile>): FqName? {
    // TODO: replace with proper main function detector, KT-44557
    val compatibleClasses = mutableListOf<FqName>()
    val visitor = object : FirVisitorVoid() {
        lateinit var file: FirFile

        override fun visitElement(element: FirElement) {}

        override fun visitFile(file: FirFile) {
            this.file = file
            file.acceptChildren(this)
        }

        override fun visitSimpleFunction(simpleFunction: FirSimpleFunction) {
            if (simpleFunction.name.asString() != "main") return
            if (simpleFunction.typeParameters.isNotEmpty()) return
            when (simpleFunction.valueParameters.size) {
                0 -> {}
                1 -> {
                    val parameterType = simpleFunction.valueParameters.single().returnTypeRef.coneType
                    if (!parameterType.isArrayType || parameterType.arrayElementType()?.isString != true) return
                }
                else -> return
            }

            compatibleClasses += FqName.fromSegments(
                file.packageFqName.pathSegments().map { it.asString() } + "${file.name.removeSuffix(".kt").capitalize()}Kt"
            )
        }
    }
    fir.forEach { it.accept(visitor) }
    return compatibleClasses.singleOrNull()
}
