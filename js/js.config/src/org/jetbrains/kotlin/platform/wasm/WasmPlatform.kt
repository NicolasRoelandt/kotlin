/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.platform.js

import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.WasmPlatform

object WasmPlatforms {
    object SimpleWasmPlatform : WasmPlatform()
    object Default : TargetPlatform(setOf(SimpleWasmPlatform))
}