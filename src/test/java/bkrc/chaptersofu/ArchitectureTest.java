package bkrc.chaptersofu;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(packages = "bkrc.chaptersofu" , importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    void 헥사고날_아키텍처_의존_규칙을_준수한다(JavaClasses classes) {
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
    void 도메인_애그리거트_사이에_순환_의존이_없다(JavaClasses classes) {
        SlicesRuleDefinition.slices()
                .matching("bkrc.chaptersofu.domain.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    @ArchTest
    void 애플리케이션_서비스_사이에_순환_의존이_없다(JavaClasses classes) {
        SlicesRuleDefinition.slices()
                .matching("bkrc.chaptersofu.application.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
