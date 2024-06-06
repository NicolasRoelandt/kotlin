/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.test.cases.generated.cases.components.resolver;

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
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.resolver.AbstractResolveSymbolTest;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi")
@TestDataPath("$PROJECT_ROOT")
public class FirIdeNormalAnalysisScriptSourceModuleResolveSymbolTestGenerated extends AbstractResolveSymbolTest {
  @NotNull
  @Override
  public AnalysisApiTestConfigurator getConfigurator() {
    return AnalysisApiFirTestConfiguratorFactory.INSTANCE.createConfigurator(
      new AnalysisApiTestConfiguratorFactoryData(
        FrontendKind.Fir,
        TestModuleKind.ScriptSource,
        AnalysisSessionMode.Normal,
        AnalysisApiMode.Ide
      )
    );
  }

  @Test
  public void testAllFilesPresentInSingleByPsi() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi"), Pattern.compile("^(.+)\\.kts$"), null, true);
  }

  @Test
  @TestMetadata("ScriptArgument.kts")
  public void testScriptArgument() {
    runTest("analysis/analysis-api/testData/components/resolver/singleByPsi/ScriptArgument.kts");
  }

  @Test
  @TestMetadata("ScriptResult.kts")
  public void testScriptResult() {
    runTest("analysis/analysis-api/testData/components/resolver/singleByPsi/ScriptResult.kts");
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/arrayAccess")
  @TestDataPath("$PROJECT_ROOT")
  public class ArrayAccess {
    @Test
    public void testAllFilesPresentInArrayAccess() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/arrayAccess"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/arrayAccess/withErrors")
    @TestDataPath("$PROJECT_ROOT")
    public class WithErrors {
      @Test
      public void testAllFilesPresentInWithErrors() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/arrayAccess/withErrors"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/callableReferences")
  @TestDataPath("$PROJECT_ROOT")
  public class CallableReferences {
    @Test
    public void testAllFilesPresentInCallableReferences() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/callableReferences"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/codeFragment")
  @TestDataPath("$PROJECT_ROOT")
  public class CodeFragment {
    @Test
    public void testAllFilesPresentInCodeFragment() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/codeFragment"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/codeFragment/blockCodeFragment")
    @TestDataPath("$PROJECT_ROOT")
    public class BlockCodeFragment {
      @Test
      public void testAllFilesPresentInBlockCodeFragment() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/codeFragment/blockCodeFragment"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/codeFragment/expressionCodeFragment")
    @TestDataPath("$PROJECT_ROOT")
    public class ExpressionCodeFragment {
      @Test
      public void testAllFilesPresentInExpressionCodeFragment() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/codeFragment/expressionCodeFragment"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/codeFragment/typeCodeFragment")
    @TestDataPath("$PROJECT_ROOT")
    public class TypeCodeFragment {
      @Test
      public void testAllFilesPresentInTypeCodeFragment() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/codeFragment/typeCodeFragment"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/constructorDelegatingReference")
  @TestDataPath("$PROJECT_ROOT")
  public class ConstructorDelegatingReference {
    @Test
    public void testAllFilesPresentInConstructorDelegatingReference() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/constructorDelegatingReference"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/danglingAnnotations")
  @TestDataPath("$PROJECT_ROOT")
  public class DanglingAnnotations {
    @Test
    public void testAllFilesPresentInDanglingAnnotations() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/danglingAnnotations"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/delegatedPropertyAccessors")
  @TestDataPath("$PROJECT_ROOT")
  public class DelegatedPropertyAccessors {
    @Test
    public void testAllFilesPresentInDelegatedPropertyAccessors() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/delegatedPropertyAccessors"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/delegatedPropertyAccessors/inSource")
    @TestDataPath("$PROJECT_ROOT")
    public class InSource {
      @Test
      public void testAllFilesPresentInInSource() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/delegatedPropertyAccessors/inSource"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/delegatedPropertyAccessors/inStandardLibrary")
    @TestDataPath("$PROJECT_ROOT")
    public class InStandardLibrary {
      @Test
      public void testAllFilesPresentInInStandardLibrary() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/delegatedPropertyAccessors/inStandardLibrary"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/delegatedPropertyAccessors/withErrors")
    @TestDataPath("$PROJECT_ROOT")
    public class WithErrors {
      @Test
      public void testAllFilesPresentInWithErrors() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/delegatedPropertyAccessors/withErrors"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn")
  @TestDataPath("$PROJECT_ROOT")
  public class ForLoopIn {
    @Test
    public void testAllFilesPresentInForLoopIn() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/inBuiltIns")
    @TestDataPath("$PROJECT_ROOT")
    public class InBuiltIns {
      @Test
      public void testAllFilesPresentInInBuiltIns() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/inBuiltIns"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/inLibrary")
    @TestDataPath("$PROJECT_ROOT")
    public class InLibrary {
      @Test
      public void testAllFilesPresentInInLibrary() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/inLibrary"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/inSource")
    @TestDataPath("$PROJECT_ROOT")
    public class InSource {
      @Test
      public void testAllFilesPresentInInSource() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/inSource"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }

      @Nested
      @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/inSource/withErrors")
      @TestDataPath("$PROJECT_ROOT")
      public class WithErrors {
        @Test
        public void testAllFilesPresentInWithErrors() {
          KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/inSource/withErrors"), Pattern.compile("^(.+)\\.kts$"), null, true);
        }
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/withErrors")
    @TestDataPath("$PROJECT_ROOT")
    public class WithErrors {
      @Test
      public void testAllFilesPresentInWithErrors() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/forLoopIn/withErrors"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/inImport")
  @TestDataPath("$PROJECT_ROOT")
  public class InImport {
    @Test
    public void testAllFilesPresentInInImport() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/inImport"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/invoke")
  @TestDataPath("$PROJECT_ROOT")
  public class Invoke {
    @Test
    public void testAllFilesPresentInInvoke() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/invoke"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/invoke/onObjects")
    @TestDataPath("$PROJECT_ROOT")
    public class OnObjects {
      @Test
      public void testAllFilesPresentInOnObjects() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/invoke/onObjects"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/invoke/withErrors")
    @TestDataPath("$PROJECT_ROOT")
    public class WithErrors {
      @Test
      public void testAllFilesPresentInWithErrors() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/invoke/withErrors"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/invokeOnObjects")
  @TestDataPath("$PROJECT_ROOT")
  public class InvokeOnObjects {
    @Test
    public void testAllFilesPresentInInvokeOnObjects() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/invokeOnObjects"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/java")
  @TestDataPath("$PROJECT_ROOT")
  public class Java {
    @Test
    public void testAllFilesPresentInJava() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/java"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/java/enumWithCustomGetName")
    @TestDataPath("$PROJECT_ROOT")
    public class EnumWithCustomGetName {
      @Test
      public void testAllFilesPresentInEnumWithCustomGetName() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/java/enumWithCustomGetName"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc")
  @TestDataPath("$PROJECT_ROOT")
  public class KDoc {
    @Test
    public void testAllFilesPresentInKDoc() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/companionObject")
    @TestDataPath("$PROJECT_ROOT")
    public class CompanionObject {
      @Test
      public void testAllFilesPresentInCompanionObject() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/companionObject"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/extensions")
    @TestDataPath("$PROJECT_ROOT")
    public class Extensions {
      @Test
      public void testAllFilesPresentInExtensions() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/extensions"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }

      @Nested
      @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/extensions/generics")
      @TestDataPath("$PROJECT_ROOT")
      public class Generics {
        @Test
        public void testAllFilesPresentInGenerics() {
          KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/extensions/generics"), Pattern.compile("^(.+)\\.kts$"), null, true);
        }
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/imports")
    @TestDataPath("$PROJECT_ROOT")
    public class Imports {
      @Test
      public void testAllFilesPresentInImports() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/imports"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/javaDeclarations")
    @TestDataPath("$PROJECT_ROOT")
    public class JavaDeclarations {
      @Test
      public void testAllFilesPresentInJavaDeclarations() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/javaDeclarations"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/localContext")
    @TestDataPath("$PROJECT_ROOT")
    public class LocalContext {
      @Test
      public void testAllFilesPresentInLocalContext() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/localContext"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/packages")
    @TestDataPath("$PROJECT_ROOT")
    public class Packages {
      @Test
      public void testAllFilesPresentInPackages() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/packages"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/parameters")
    @TestDataPath("$PROJECT_ROOT")
    public class Parameters {
      @Test
      public void testAllFilesPresentInParameters() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/parameters"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified")
    @TestDataPath("$PROJECT_ROOT")
    public class Qualified {
      @Test
      public void testAllFilesPresentInQualified() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }

      @Nested
      @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/callables")
      @TestDataPath("$PROJECT_ROOT")
      public class Callables {
        @Test
        public void testAllFilesPresentInCallables() {
          KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/callables"), Pattern.compile("^(.+)\\.kts$"), null, true);
        }

        @Nested
        @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/callables/notImported")
        @TestDataPath("$PROJECT_ROOT")
        public class NotImported {
          @Test
          public void testAllFilesPresentInNotImported() {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/callables/notImported"), Pattern.compile("^(.+)\\.kts$"), null, true);
          }
        }
      }

      @Nested
      @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/conflictResolution")
      @TestDataPath("$PROJECT_ROOT")
      public class ConflictResolution {
        @Test
        public void testAllFilesPresentInConflictResolution() {
          KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/conflictResolution"), Pattern.compile("^(.+)\\.kts$"), null, true);
        }
      }

      @Nested
      @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/fromOtherFile")
      @TestDataPath("$PROJECT_ROOT")
      public class FromOtherFile {
        @Test
        public void testAllFilesPresentInFromOtherFile() {
          KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/fromOtherFile"), Pattern.compile("^(.+)\\.kts$"), null, true);
        }
      }

      @Nested
      @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/stdlib")
      @TestDataPath("$PROJECT_ROOT")
      public class Stdlib {
        @Test
        public void testAllFilesPresentInStdlib() {
          KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/qualified/stdlib"), Pattern.compile("^(.+)\\.kts$"), null, true);
        }
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/thisQualifier")
    @TestDataPath("$PROJECT_ROOT")
    public class ThisQualifier {
      @Test
      public void testAllFilesPresentInThisQualifier() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/thisQualifier"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/withErrors")
    @TestDataPath("$PROJECT_ROOT")
    public class WithErrors {
      @Test
      public void testAllFilesPresentInWithErrors() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kDoc/withErrors"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/kotlinPackage")
  @TestDataPath("$PROJECT_ROOT")
  public class KotlinPackage {
    @Test
    public void testAllFilesPresentInKotlinPackage() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/kotlinPackage"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/nestedTypes")
  @TestDataPath("$PROJECT_ROOT")
  public class NestedTypes {
    @Test
    public void testAllFilesPresentInNestedTypes() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/nestedTypes"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/nonCalls")
  @TestDataPath("$PROJECT_ROOT")
  public class NonCalls {
    @Test
    public void testAllFilesPresentInNonCalls() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/nonCalls"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/operators")
  @TestDataPath("$PROJECT_ROOT")
  public class Operators {
    @Test
    public void testAllFilesPresentInOperators() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/operators"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/operators/assignment")
    @TestDataPath("$PROJECT_ROOT")
    public class Assignment {
      @Test
      public void testAllFilesPresentInAssignment() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/operators/assignment"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/operators/compareTo")
    @TestDataPath("$PROJECT_ROOT")
    public class CompareTo {
      @Test
      public void testAllFilesPresentInCompareTo() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/operators/compareTo"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/operators/contains")
    @TestDataPath("$PROJECT_ROOT")
    public class Contains {
      @Test
      public void testAllFilesPresentInContains() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/operators/contains"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/operators/equals")
    @TestDataPath("$PROJECT_ROOT")
    public class Equals {
      @Test
      public void testAllFilesPresentInEquals() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/operators/equals"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/packageReference")
  @TestDataPath("$PROJECT_ROOT")
  public class PackageReference {
    @Test
    public void testAllFilesPresentInPackageReference() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/packageReference"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/qualifiedAccess")
  @TestDataPath("$PROJECT_ROOT")
  public class QualifiedAccess {
    @Test
    public void testAllFilesPresentInQualifiedAccess() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/qualifiedAccess"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeAlias")
  @TestDataPath("$PROJECT_ROOT")
  public class TypeAlias {
    @Test
    public void testAllFilesPresentInTypeAlias() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeAlias"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeAlias/missingDependency")
    @TestDataPath("$PROJECT_ROOT")
    public class MissingDependency {
      @Test
      public void testAllFilesPresentInMissingDependency() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeAlias/missingDependency"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeAlias/withErrors")
    @TestDataPath("$PROJECT_ROOT")
    public class WithErrors {
      @Test
      public void testAllFilesPresentInWithErrors() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeAlias/withErrors"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument")
  @TestDataPath("$PROJECT_ROOT")
  public class TypeArgument {
    @Test
    public void testAllFilesPresentInTypeArgument() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument/constant")
    @TestDataPath("$PROJECT_ROOT")
    public class Constant {
      @Test
      public void testAllFilesPresentInConstant() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument/constant"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument/functionCall")
    @TestDataPath("$PROJECT_ROOT")
    public class FunctionCall {
      @Test
      public void testAllFilesPresentInFunctionCall() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument/functionCall"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument/propertyAccess")
    @TestDataPath("$PROJECT_ROOT")
    public class PropertyAccess {
      @Test
      public void testAllFilesPresentInPropertyAccess() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument/propertyAccess"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument/type")
    @TestDataPath("$PROJECT_ROOT")
    public class Type {
      @Test
      public void testAllFilesPresentInType() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeArgument/type"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/typeParameter")
  @TestDataPath("$PROJECT_ROOT")
  public class TypeParameter {
    @Test
    public void testAllFilesPresentInTypeParameter() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/typeParameter"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/withErrors")
  @TestDataPath("$PROJECT_ROOT")
  public class WithErrors {
    @Test
    public void testAllFilesPresentInWithErrors() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/withErrors"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/withErrors/partiallyUnresolvedTypeQualifier")
    @TestDataPath("$PROJECT_ROOT")
    public class PartiallyUnresolvedTypeQualifier {
      @Test
      public void testAllFilesPresentInPartiallyUnresolvedTypeQualifier() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/withErrors/partiallyUnresolvedTypeQualifier"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/resolver/singleByPsi/withTestCompilerPluginEnabled")
  @TestDataPath("$PROJECT_ROOT")
  public class WithTestCompilerPluginEnabled {
    @Test
    public void testAllFilesPresentInWithTestCompilerPluginEnabled() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/resolver/singleByPsi/withTestCompilerPluginEnabled"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }
}
