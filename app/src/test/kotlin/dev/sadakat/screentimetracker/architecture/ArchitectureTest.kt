package dev.sadakat.screentimetracker.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture

@AnalyzeClasses(
    packages = ["dev.sadakat.screentimetracker"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class ArchitectureTest {

    @ArchTest
    val layerDependencies = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Presentation").definedBy("..presentation..", "..feature..")
        .layer("Domain").definedBy("..domain..")
        .layer("Data").definedBy("..data..")
        .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Presentation", "Data")
        .whereLayer("Data").mayOnlyBeAccessedByLayers("Presentation")

    @ArchTest
    val domainShouldNotDependOnFramework = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("android..", "androidx..")
        .because("Domain layer must be framework independent")

    @ArchTest
    val useCaseNaming = classes()
        .that().resideInAPackage("..domain.usecase..")
        .should().haveSimpleNameEndingWith("UseCase")
        .because("Use cases should follow naming convention")

    @ArchTest
    val repositoryImplementations = classes()
        .that().implement("..domain.repository..")
        .should().resideInAPackage("..data.repository..")
        .because("Repository implementations belong in data layer")

    @ArchTest
    val viewModelsDependencies = classes()
        .that().haveSimpleNameEndingWith("ViewModel")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..domain..",
            "androidx.lifecycle..",
            "kotlinx.coroutines..",
            "javax.inject..",
            "dagger.hilt..",
            "kotlin..",
            "java.."
        ).because("ViewModels should only depend on domain layer")

    @ArchTest
    val entitiesShouldBeInDomain = classes()
        .that().haveSimpleNameEndingWith("Entity")
        .should().resideInAPackage("..domain.entity..")
        .because("Entities should be in domain layer")

    @ArchTest
    val modelsShouldBeInData = classes()
        .that().haveSimpleNameEndingWith("Model")
        .or().haveSimpleNameEndingWith("Dto")
        .should().resideInAnyPackage("..data..", "..network..", "..local..")
        .because("Models and DTOs should be in data layer")
}