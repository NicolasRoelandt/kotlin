/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test.blackbox;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.konan.test.blackbox.support.EnforcedProperty;
import org.jetbrains.kotlin.konan.test.blackbox.support.ClassLevelProperty;
import org.junit.jupiter.api.Tag;
import org.jetbrains.kotlin.konan.test.blackbox.support.group.FirPipeline;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.GenerateNativeTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("native/native.tests/testData/CExport/InterfaceV1")
@TestDataPath("$PROJECT_ROOT")
@EnforcedProperty(property = ClassLevelProperty.BINARY_LIBRARY_KIND, propertyValue = "STATIC")
@EnforcedProperty(property = ClassLevelProperty.C_INTERFACE_MODE, propertyValue = "V1")
@Tag("frontend-fir")
@FirPipeline()
public class FirCExportStaticInterfaceV1TestGenerated extends AbstractNativeCExportTest {
  @Test
  public void testAllFilesPresentInInterfaceV1() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("native/native.tests/testData/CExport/InterfaceV1"), Pattern.compile("^([^_](.+))$"), null, false);
  }

  @Test
  @TestMetadata("concurrentTerminate")
  public void testConcurrentTerminate() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/concurrentTerminate/");
  }

  @Test
  @TestMetadata("kt36639")
  public void testKt36639() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt36639/");
  }

  @Test
  @TestMetadata("kt42397")
  public void testKt42397() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt42397/");
  }

  @Test
  @TestMetadata("kt56182_package1lvl")
  public void testKt56182_package1lvl() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt56182_package1lvl/");
  }

  @Test
  @TestMetadata("kt56182_root")
  public void testKt56182_root() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt56182_root/");
  }

  @Test
  @TestMetadata("kt56182_root_package1lvl")
  public void testKt56182_root_package1lvl() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt56182_root_package1lvl/");
  }

  @Test
  @TestMetadata("kt56182_root_subpackage2lvl")
  public void testKt56182_root_subpackage2lvl() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt56182_root_subpackage2lvl/");
  }

  @Test
  @TestMetadata("kt56182_subpackage2lvl")
  public void testKt56182_subpackage2lvl() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt56182_subpackage2lvl/");
  }

  @Test
  @TestMetadata("kt-36878")
  public void testKt_36878() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-36878/");
  }

  @Test
  @TestMetadata("kt-39015")
  public void testKt_39015() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-39015/");
  }

  @Test
  @TestMetadata("kt-39496")
  public void testKt_39496() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-39496/");
  }

  @Test
  @TestMetadata("kt-41904")
  public void testKt_41904() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-41904/");
  }

  @Test
  @TestMetadata("kt-42796-0")
  public void testKt_42796_0() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-42796-0/");
  }

  @Test
  @TestMetadata("kt-42796-1")
  public void testKt_42796_1() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-42796-1/");
  }

  @Test
  @TestMetadata("kt-42796-2")
  public void testKt_42796_2() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-42796-2/");
  }

  @Test
  @TestMetadata("kt-42830")
  public void testKt_42830() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-42830/");
  }

  @Test
  @TestMetadata("kt-64508")
  public void testKt_64508() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/kt-64508/");
  }

  @Test
  @TestMetadata("migrating_main_thread")
  public void testMigrating_main_thread() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/migrating_main_thread/");
  }

  @Test
  @TestMetadata("programName")
  public void testProgramName() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/programName/");
  }

  @Test
  @TestMetadata("simple")
  public void testSimple() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/simple/");
  }

  @Test
  @TestMetadata("smoke0")
  public void testSmoke0() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/smoke0/");
  }

  @Test
  @TestMetadata("unhandledException")
  public void testUnhandledException() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/unhandledException/");
  }

  @Test
  @TestMetadata("unhandledExceptionThroughBridge")
  public void testUnhandledExceptionThroughBridge() {
    runTest("native/native.tests/testData/CExport/InterfaceV1/unhandledExceptionThroughBridge/");
  }
}
