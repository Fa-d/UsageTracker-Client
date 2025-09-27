import io.gitlab.arturbosch.detekt.Detekt

// Register custom architecture validation tasks
tasks.register<ValidateArchitectureTask>("validateArchitecture") {
    dependsOn("detekt", "test")
}

tasks.register<GenerateArchitectureReportTask>("generateArchitectureReport") {
    dependsOn("validateArchitecture")
}

// Configure Detekt for architecture enforcement
plugins.withId("io.gitlab.arturbosch.detekt") {
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        // Custom rules configuration
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false

        // Include architecture rules module
        dependencies {
            detektPlugins(project(":architecture-rules"))
        }
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }

        // Fail build on architecture violations
        doLast {
            val reportFile = file("${project.buildDir}/reports/detekt/detekt.xml")
            if (reportFile.exists()) {
                val content = reportFile.readText()
                if (content.contains("CleanArchitectureViolation") ||
                    content.contains("DomainLayerImpurity")) {
                    throw GradleException("❌ Architecture violations detected! Check the Detekt report.")
                }
            }
        }
    }
}

// Merge Detekt SARIF reports for GitHub Actions
tasks.register("mergeDetektSarif") {
    doLast {
        val reportDir = file("${project.buildDir}/reports/detekt")
        reportDir.mkdirs()

        val allSarifFiles = fileTree(rootDir) {
            include("**/build/reports/detekt/*.sarif")
        }

        if (allSarifFiles.files.isNotEmpty()) {
            val mergedSarif = file("$reportDir/merged.sarif")
            allSarifFiles.files.first().copyTo(mergedSarif, overwrite = true)
            println("📄 Merged SARIF report: $mergedSarif")
        }
    }
}

// Pre-build validation for CI/CD
tasks.named("preBuild") {
    if (System.getenv("CI") == "true" ||
        gradle.startParameter.taskNames.contains("assembleRelease") ||
        gradle.startParameter.taskNames.contains("bundleRelease")) {
        dependsOn("validateArchitecture")
    }
}

// Architecture validation on test task
tasks.withType<Test> {
    if (name.contains("Architecture")) {
        // Set system properties for ArchUnit
        systemProperty("archunit.fail_on_empty_should_clause", "false")
        systemProperty("archunit.ignore_missing_classes", "true")

        // Increase heap size for large codebases
        maxHeapSize = "2g"

        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = false
        }
    }
}