package dev.sadakat.screentimetracker.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

@AnalyzeClasses(
    packages = ["dev.sadakat.screentimetracker"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class ModuleBoundaryTest {

    @ArchTest
    val featureIndependence = noClasses()
        .that().resideInAPackage("..feature.login..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..feature.home..", "..feature.dashboard..", "..feature.analytics..", "..feature.wellness..", "..feature.goals..", "..feature.settings..")
        .because("Features should be independent")

    @ArchTest
    val homeFeatureIndependence = noClasses()
        .that().resideInAPackage("..feature.home..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..feature.login..", "..feature.dashboard..", "..feature.analytics..", "..feature.wellness..", "..feature.goals..", "..feature.settings..")
        .because("Features should be independent")

    @ArchTest
    val dashboardFeatureIndependence = noClasses()
        .that().resideInAPackage("..feature.dashboard..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..feature.login..", "..feature.home..", "..feature.analytics..", "..feature.wellness..", "..feature.goals..", "..feature.settings..")
        .because("Features should be independent")

    @ArchTest
    val analyticsFeatureIndependence = noClasses()
        .that().resideInAPackage("..feature.analytics..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..feature.login..", "..feature.home..", "..feature.dashboard..", "..feature.wellness..", "..feature.goals..", "..feature.settings..")
        .because("Features should be independent")

    @ArchTest
    val wellnessFeatureIndependence = noClasses()
        .that().resideInAPackage("..feature.wellness..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..feature.login..", "..feature.home..", "..feature.dashboard..", "..feature.analytics..", "..feature.goals..", "..feature.settings..")
        .because("Features should be independent")

    @ArchTest
    val goalsFeatureIndependence = noClasses()
        .that().resideInAPackage("..feature.goals..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..feature.login..", "..feature.home..", "..feature.dashboard..", "..feature.analytics..", "..feature.wellness..", "..feature.settings..")
        .because("Features should be independent")

    @ArchTest
    val settingsFeatureIndependence = noClasses()
        .that().resideInAPackage("..feature.settings..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..feature.login..", "..feature.home..", "..feature.dashboard..", "..feature.analytics..", "..feature.wellness..", "..feature.goals..")
        .because("Features should be independent")

    @ArchTest
    val dataSourceIsolation = noClasses()
        .that().resideInAPackage("..presentation..")
        .should().directlyDependOnClassesThat()
        .resideInAnyPackage("..data.local..", "..data.remote..")
        .because("Presentation should access data through repositories")

    @ArchTest
    val coreModulesShouldNotDependOnFeatures = noClasses()
        .that().resideInAPackage("..core..")
        .should().dependOnClassesThat()
        .resideInAPackage("..feature..")
        .because("Core modules should not depend on feature modules")

    @ArchTest
    val dataModulesShouldNotDependOnFeatures = noClasses()
        .that().resideInAPackage("..data..")
        .should().dependOnClassesThat()
        .resideInAPackage("..feature..")
        .because("Data modules should not depend on feature modules")

    @ArchTest
    val domainModulesShouldNotDependOnFeatures = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("..feature..")
        .because("Domain modules should not depend on feature modules")
}