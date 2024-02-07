/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test

import com.intellij.testFramework.TestDataPath
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.test.blackbox.AbstractNativeSimpleTest
import org.jetbrains.kotlin.konan.test.blackbox.support.NativeSimpleTestSupport
import org.jetbrains.kotlin.konan.test.blackbox.support.settings.Binaries
import org.jetbrains.kotlin.konan.test.blackbox.support.settings.GCScheduler
import org.jetbrains.kotlin.konan.test.blackbox.support.settings.KotlinNativeHome
import org.jetbrains.kotlin.konan.test.blackbox.targets
import org.jetbrains.kotlin.native.executors.RunProcessResult
import org.jetbrains.kotlin.native.executors.runProcess
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import kotlin.test.assertFalse
import kotlin.time.Duration

@Tag("driver")
@TestDataPath("\$PROJECT_ROOT")
@ExtendWith(NativeSimpleTestSupport::class)
class KonanDriverTest : AbstractNativeSimpleTest() {
    private val konanHome get() = testRunSettings.get<KotlinNativeHome>().dir
    private val buildDir get() = testRunSettings.get<Binaries>().testBinariesDir
    private val konanc get() = konanHome.resolve("bin").resolve(if (HostManager.hostIsMingw) "konanc.bat" else "konanc")

    private val testSuiteDir = File("native/native.tests/testData/driver")

    @Test
    fun testLLVMVariantDev() {
        // We use clang from Xcode on macOS for apple targets,
        // so it is hard to reliably detect LLVM variant for these targets.
//        Assumptions.assumeFalse(targets.hostTarget.family.isAppleFamily && targets.testTarget.family.isAppleFamily)
        // No need to test with different GC schedulers
        Assumptions.assumeFalse(testRunSettings.get<GCScheduler>() == GCScheduler.AGGRESSIVE)

        val source = testSuiteDir.resolve("hello.kt")
        val flags = listOf("-Xllvm-variant=dev", "-Xverbose-phases=ObjectFiles")
        val args = mutableListOf("-output", buildDir.resolve("kexe.kexe").absolutePath).apply {
            add("-target")
            add(targets.testTarget.visibleName)
            addAll(flags)

            add("-Xpartial-linkage=enable")
            add("-Xpartial-linkage-loglevel=error")
        }

        val result: RunProcessResult = runProcess(konanc.absolutePath, source.absolutePath, *args.toTypedArray()) {
            timeout = Duration.parse("5m")
        }
        assertFalse(
            result.stdout.contains("-essentials"),
            "`-essentials` must not be in stdout of dev LLVM.\nSTDOUT: ${result.stdout}\nSTDERR: ${result.stderr}\n---"
        )
    }
}
