/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/codegen/script")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ScriptCodegenTestGenerated extends AbstractScriptCodegenTest {
    @TestMetadata("adder.kts")
    public void testAdder() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/adder.kts");
        doTest(fileName);
    }

    public void testAllFilesPresentInScript() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("compiler/testData/codegen/script"), Pattern.compile("^(.+)\\.kts$"), TargetBackend.ANY, true);
    }

    @TestMetadata("classLiteralInsideFunction.kts")
    public void testClassLiteralInsideFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/classLiteralInsideFunction.kts");
        doTest(fileName);
    }

    @TestMetadata("destructuringDeclaration.kts")
    public void testDestructuringDeclaration() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/destructuringDeclaration.kts");
        doTest(fileName);
    }

    @TestMetadata("destructuringDeclarationUnderscore.kts")
    public void testDestructuringDeclarationUnderscore() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/destructuringDeclarationUnderscore.kts");
        doTest(fileName);
    }

    @TestMetadata("empty.kts")
    public void testEmpty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/empty.kts");
        doTest(fileName);
    }

    @TestMetadata("helloWorld.kts")
    public void testHelloWorld() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/helloWorld.kts");
        doTest(fileName);
    }

    @TestMetadata("inline.kts")
    public void testInline() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/inline.kts");
        doTest(fileName);
    }

    @TestMetadata("kt20707.kts")
    public void testKt20707() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/kt20707.kts");
        doTest(fileName);
    }

    @TestMetadata("kt22029.kts")
    public void testKt22029() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/kt22029.kts");
        doTest(fileName);
    }

    @TestMetadata("localDelegatedProperty.kts")
    public void testLocalDelegatedProperty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/localDelegatedProperty.kts");
        doTest(fileName);
    }

    @TestMetadata("localDelegatedPropertyNoExplicitType.kts")
    public void testLocalDelegatedPropertyNoExplicitType() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/localDelegatedPropertyNoExplicitType.kts");
        doTest(fileName);
    }

    @TestMetadata("localFunction.kts")
    public void testLocalFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/localFunction.kts");
        doTest(fileName);
    }

    @TestMetadata("outerCapture.kts")
    public void testOuterCapture() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/outerCapture.kts");
        doTest(fileName);
    }

    @TestMetadata("parameter.kts")
    public void testParameter() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/parameter.kts");
        doTest(fileName);
    }

    @TestMetadata("parameterArray.kts")
    public void testParameterArray() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/parameterArray.kts");
        doTest(fileName);
    }

    @TestMetadata("parameterClosure.kts")
    public void testParameterClosure() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/parameterClosure.kts");
        doTest(fileName);
    }

    @TestMetadata("parameterLong.kts")
    public void testParameterLong() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/parameterLong.kts");
        doTest(fileName);
    }

    @TestMetadata("secondLevelFunction.kts")
    public void testSecondLevelFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/secondLevelFunction.kts");
        doTest(fileName);
    }

    @TestMetadata("secondLevelFunctionClosure.kts")
    public void testSecondLevelFunctionClosure() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/secondLevelFunctionClosure.kts");
        doTest(fileName);
    }

    @TestMetadata("secondLevelVal.kts")
    public void testSecondLevelVal() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/secondLevelVal.kts");
        doTest(fileName);
    }

    @TestMetadata("simpleClass.kts")
    public void testSimpleClass() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/simpleClass.kts");
        doTest(fileName);
    }

    @TestMetadata("string.kts")
    public void testString() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/string.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelFunction.kts")
    public void testTopLevelFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelFunction.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelFunctionClosure.kts")
    public void testTopLevelFunctionClosure() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelFunctionClosure.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelLocalDelegatedProperty.kts")
    public void testTopLevelLocalDelegatedProperty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelLocalDelegatedProperty.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelPropertiesWithGetSet.kts")
    public void testTopLevelPropertiesWithGetSet() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelPropertiesWithGetSet.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelProperty.kts")
    public void testTopLevelProperty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelProperty.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelPropertyWithProvideDelegate.kts")
    public void testTopLevelPropertyWithProvideDelegate() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelPropertyWithProvideDelegate.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelTypealias.kts")
    public void testTopLevelTypealias() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelTypealias.kts");
        doTest(fileName);
    }
}
