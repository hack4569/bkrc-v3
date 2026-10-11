package bkrc.chaptersofu.adapter.tests;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(packages = "bkrc.chaptersofu" , importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    void hexagonalArchitecture(JavaClasses classes) {
        Architectures.layeredArchitecture()
                .consideringAllDependencies()
                .layer("domain").definedBy("bkrc.chaptersofu.domain..")
                .layer("application").definedBy("bkrc.chaptersofu.application..")
                .layer("adapter").definedBy("bkrc.chaptersofu.adapter..")
                .whereLayer("domain").mayOnlyBeAccessedByLayers("application", "adapter")
                .whereLayer("application").mayOnlyBeAccessedByLayers("adapter")
                .whereLayer("adapter").mayNotBeAccessedByAnyLayer()
                .check(classes);
    }

    @ArchTest
    void aggregateFreeOfCycles(JavaClasses classes) {
        SlicesRuleDefinition.slices()
                .matching("bkrc.chaptersofu.domain.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    @ArchTest
    void applicationServiceFreeOfCycles(JavaClasses classes) {
        SlicesRuleDefinition.slices()
                .matching("bkrc.chaptersofu.application.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
