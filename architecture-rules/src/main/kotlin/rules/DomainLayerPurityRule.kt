package com.usagetracker.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtImportDirective

class DomainLayerPurityRule(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = "DomainLayerImpurity",
        severity = Severity.Maintainability,
        description = "Domain layer must not depend on Android framework",
        debt = Debt.TEN_MINS
    )

    private val forbiddenImports = listOf(
        "android.",
        "androidx.",
        "kotlinx.android.",
        "javax.inject.Inject", // Use domain-specific injection
        "dagger.",
        "hilt."
    )

    override fun visitImportDirective(import: KtImportDirective) {
        val importPath = import.importPath?.pathStr ?: return
        val file = import.containingKtFile

        // Check if we're in domain layer
        val packageName = file.packageFqName.asString()
        if (!packageName.contains("dev.sadakat.screentimetracker.domain") &&
            !packageName.contains(".domain.")) {
            return
        }

        forbiddenImports.forEach { forbidden ->
            if (importPath.startsWith(forbidden)) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(import),
                        "Domain layer cannot depend on '$importPath'. " +
                        "Domain must be pure Kotlin/Java."
                    )
                )
            }
        }
    }
}