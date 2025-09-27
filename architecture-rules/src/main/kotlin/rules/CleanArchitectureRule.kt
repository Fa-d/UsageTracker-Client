package com.usagetracker.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective

class CleanArchitectureRule(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = "CleanArchitectureViolation",
        severity = Severity.Maintainability,
        description = "Ensures clean architecture layer dependencies",
        debt = Debt.FIVE_MINS
    )

    private val layerPatterns = mapOf(
        "domain" to listOf("domain"),
        "data" to listOf("domain", "data", "core"),
        "feature" to listOf("domain", "data", "feature", "core"),
        "core" to listOf("core")
    )

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)

        val currentLayer = detectLayer(file.packageFqName.asString())
        if (currentLayer == null) return

        file.importList?.imports?.forEach { import ->
            checkImport(import, currentLayer)
        }
    }

    private fun detectLayer(packageName: String): String? {
        return when {
            packageName.contains("dev.sadakat.screentimetracker.domain") ||
            packageName.contains(".domain.") -> "domain"
            packageName.contains("dev.sadakat.screentimetracker.data") ||
            packageName.contains(".data.") -> "data"
            packageName.contains("dev.sadakat.screentimetracker.feature") ||
            packageName.contains(".feature.") -> "feature"
            packageName.contains("dev.sadakat.screentimetracker.core") ||
            packageName.contains(".core.") -> "core"
            else -> null
        }
    }

    private fun checkImport(import: KtImportDirective, currentLayer: String) {
        val importPath = import.importPath?.pathStr ?: return
        val importLayer = detectLayer(importPath) ?: return

        val allowedLayers = layerPatterns[currentLayer] ?: return

        if (!allowedLayers.contains(importLayer)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(import),
                    "Layer '$currentLayer' should not depend on '$importLayer'. " +
                    "Allowed dependencies: ${allowedLayers.joinToString()}"
                )
            )
        }
    }
}