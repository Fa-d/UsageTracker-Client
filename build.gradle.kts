// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

// Apply git hooks
apply(from = "gradle/git-hooks.gradle.kts")

// Configure Detekt for the entire project
detekt {
    config.setFrom(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = false
    source = files(
        "app/src/main/java",
        "core/*/src/main/java",
        "data/*/src/main/java",
        "domain/*/src/main/java",
        "feature/*/src/main/java"
    )
}

dependencies {
    detektPlugins(project(":architecture-rules"))
}

// Global tasks
tasks.register("cleanBuildCache") {
    doLast {
        delete("$rootDir/.gradle/caches")
        delete("$rootDir/build")
        println("✅ Build cache cleaned")
    }
}

tasks.register("validateArchitecture") {
    group = "verification"
    description = "Validates clean architecture compliance"
    dependsOn("detekt")
    doLast {
        println("✅ Architecture validation complete")
    }
}

tasks.register("setupProject") {
    dependsOn("installGitHooks")
    doLast {
        println("🎯 Project setup complete!")
        println("Run './gradlew validateArchitecture' to check architecture compliance")
    }
}

// Print architecture validation status on build
gradle.taskGraph.whenReady {
    println("\n🏗️ Build configured successfully!")
    println("💡 Run './gradlew validateArchitecture' to ensure clean architecture compliance")
}