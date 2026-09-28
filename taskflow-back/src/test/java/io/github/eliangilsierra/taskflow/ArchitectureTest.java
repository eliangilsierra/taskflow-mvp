package io.github.eliangilsierra.taskflow;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Executable version of the dependency rules described in docs/architecture.md. */
@AnalyzeClasses(
    packages = "io.github.eliangilsierra.taskflow",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final String BASE = "io.github.eliangilsierra.taskflow";

  @ArchTest
  static final ArchRule domainIsFreeOfFrameworksAndOuterLayers =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "jakarta..",
              "org.hibernate..",
              "..application..",
              "..infrastructure..",
              "..api..")
          .because("the domain must stay independent of frameworks and outer layers");

  @ArchTest
  static final ArchRule applicationDoesNotKnowAdaptersOrTheWebLayer =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..infrastructure..", "..api..");

  @ArchTest
  static final ArchRule apiDoesNotUseInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..api..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure..");

  @ArchTest
  static final ArchRule infrastructureDoesNotUseTheWebLayer =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..api..");

  @ArchTest
  static final ArchRule featuresAreIndependent =
      noClasses()
          .that()
          .resideInAPackage(BASE + ".tasks..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage(BASE + ".auth..")
          .because("tasks only need the caller's id, which the shared kernel provides");

  @ArchTest
  static final ArchRule sharedKernelDoesNotDependOnFeatures =
      noClasses()
          .that()
          .resideInAPackage(BASE + ".shared..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(BASE + ".auth..", BASE + ".tasks..");
}
