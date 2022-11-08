/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.test.cases.generated.cases.components.expressionTypeProvider;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analysis.api.fir.test.configurators.AnalysisApiFirTestConfiguratorFactory;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfiguratorFactoryData;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.TestModuleKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.FrontendKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisSessionMode;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiMode;
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.expressionTypeProvider.AbstractHLExpressionTypeTest;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType")
@TestDataPath("$PROJECT_ROOT")
public class FirIdeDependentAnalysisSourceModuleHLExpressionTypeTestGenerated extends AbstractHLExpressionTypeTest {
    @NotNull
    @Override
    public AnalysisApiTestConfigurator getConfigurator() {
        return AnalysisApiFirTestConfiguratorFactory.INSTANCE.createConfigurator(
            new AnalysisApiTestConfiguratorFactoryData(
                FrontendKind.Fir,
                TestModuleKind.Source,
                AnalysisSessionMode.Dependent,
                AnalysisApiMode.Ide
            )
        );
    }

    @Test
    public void testAllFilesPresentInExpressionType() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @Test
    @TestMetadata("anonymousFunction.kt")
    public void testAnonymousFunction() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/anonymousFunction.kt");
    }

    @Test
    @TestMetadata("arrayElement_arrayOfNulls.kt")
    public void testArrayElement_arrayOfNulls() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/arrayElement_arrayOfNulls.kt");
    }

    @Test
    @TestMetadata("array_arrayOfNulls.kt")
    public void testArray_arrayOfNulls() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/array_arrayOfNulls.kt");
    }

    @Test
    @TestMetadata("assignmentExpressionTarget.kt")
    public void testAssignmentExpressionTarget() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/assignmentExpressionTarget.kt");
    }

    @Test
    @TestMetadata("binaryExpression.kt")
    public void testBinaryExpression() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/binaryExpression.kt");
    }

    @Test
    @TestMetadata("breakExpression.kt")
    public void testBreakExpression() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/breakExpression.kt");
    }

    @Test
    @TestMetadata("forExpression.kt")
    public void testForExpression() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/forExpression.kt");
    }

    @Test
    @TestMetadata("functionCall.kt")
    public void testFunctionCall() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/functionCall.kt");
    }

    @Test
    @TestMetadata("inParens.kt")
    public void testInParens() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/inParens.kt");
    }

    @Test
    @TestMetadata("insideStringTemplate.kt")
    public void testInsideStringTemplate() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/insideStringTemplate.kt");
    }

    @Test
    @TestMetadata("insideStringTemplateWithBinrary.kt")
    public void testInsideStringTemplateWithBinrary() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/insideStringTemplateWithBinrary.kt");
    }

    @Test
    @TestMetadata("intLiteral.kt")
    public void testIntLiteral() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/intLiteral.kt");
    }

    @Test
    @TestMetadata("listElement_listOf.kt")
    public void testListElement_listOf() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/listElement_listOf.kt");
    }

    @Test
    @TestMetadata("listElement_mutableListOf.kt")
    public void testListElement_mutableListOf() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/listElement_mutableListOf.kt");
    }

    @Test
    @TestMetadata("list_listOf.kt")
    public void testList_listOf() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/list_listOf.kt");
    }

    @Test
    @TestMetadata("list_mutableListOf.kt")
    public void testList_mutableListOf() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/list_mutableListOf.kt");
    }

    @Test
    @TestMetadata("nonExpression.kt")
    public void testNonExpression() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/nonExpression.kt");
    }

    @Test
    @TestMetadata("platformType.kt")
    public void testPlatformType() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/platformType.kt");
    }

    @Test
    @TestMetadata("plusAssign.kt")
    public void testPlusAssign() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/plusAssign.kt");
    }

    @Test
    @TestMetadata("postfixDec.kt")
    public void testPostfixDec() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/postfixDec.kt");
    }

    @Test
    @TestMetadata("prefixInc.kt")
    public void testPrefixInc() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/prefixInc.kt");
    }

    @Test
    @TestMetadata("property.kt")
    public void testProperty() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/property.kt");
    }

    @Test
    @TestMetadata("resolvedSuper.kt")
    public void testResolvedSuper() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/resolvedSuper.kt");
    }

    @Test
    @TestMetadata("returnExpression.kt")
    public void testReturnExpression() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/returnExpression.kt");
    }

    @Test
    @TestMetadata("singleExpressionLambdaBody.kt")
    public void testSingleExpressionLambdaBody() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/singleExpressionLambdaBody.kt");
    }

    @Test
    @TestMetadata("smartcast_asCallArg.kt")
    public void testSmartcast_asCallArg() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/smartcast_asCallArg.kt");
    }

    @Test
    @TestMetadata("smartcast_asReceiver.kt")
    public void testSmartcast_asReceiver() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/smartcast_asReceiver.kt");
    }

    @Test
    @TestMetadata("smartcast_multi.kt")
    public void testSmartcast_multi() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/smartcast_multi.kt");
    }

    @Test
    @TestMetadata("smartcast_unused.kt")
    public void testSmartcast_unused() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/smartcast_unused.kt");
    }

    @Test
    @TestMetadata("stringLiteral.kt")
    public void testStringLiteral() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/stringLiteral.kt");
    }

    @Test
    @TestMetadata("unresolvedSuper_multipleSuperTypes.kt")
    public void testUnresolvedSuper_multipleSuperTypes() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/unresolvedSuper_multipleSuperTypes.kt");
    }

    @Test
    @TestMetadata("unresolvedSuper_noSuperType.kt")
    public void testUnresolvedSuper_noSuperType() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/unresolvedSuper_noSuperType.kt");
    }

    @Test
    @TestMetadata("unresolvedSuper_singleSuperType.kt")
    public void testUnresolvedSuper_singleSuperType() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/unresolvedSuper_singleSuperType.kt");
    }

    @Test
    @TestMetadata("whileExpression.kt")
    public void testWhileExpression() throws Exception {
        runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/whileExpression.kt");
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/assignment")
    @TestDataPath("$PROJECT_ROOT")
    public class Assignment {
        @Test
        public void testAllFilesPresentInAssignment() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/assignment"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("arrayAssignementTarget.kt")
        public void testArrayAssignementTarget() throws Exception {
            runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/assignment/arrayAssignementTarget.kt");
        }

        @Test
        @TestMetadata("arrayAssignmentTargetUnresovledSet.kt")
        public void testArrayAssignmentTargetUnresovledSet() throws Exception {
            runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/assignment/arrayAssignmentTargetUnresovledSet.kt");
        }

        @Test
        @TestMetadata("arrayCompoundAssignementTarget.kt")
        public void testArrayCompoundAssignementTarget() throws Exception {
            runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/assignment/arrayCompoundAssignementTarget.kt");
        }

        @Test
        @TestMetadata("readArrayElement.kt")
        public void testReadArrayElement() throws Exception {
            runTest("analysis/analysis-api/testData/components/expressionTypeProvider/expressionType/assignment/readArrayElement.kt");
        }
    }
}
