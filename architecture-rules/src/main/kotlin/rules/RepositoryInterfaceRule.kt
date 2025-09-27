package com.usagetracker.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtClass

class RepositoryInterfaceRule(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = "RepositoryInterface",
        severity = Severity.Maintainability,
        description = "Repository interfaces should be in domain layer",
        debt = Debt.FIVE_MINS
    )

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        val className = klass.name ?: return
        val file = klass.containingKtFile
        val packageName = file.packageFqName.asString()

        // Check if this is a repository interface
        if (!className.endsWith("Repository") || !klass.isInterface()) {
            return
        }

        // Repository interfaces should be in domain layer
        if (!packageName.contains("dev.sadakat.screentimetracker.domain") &&
            !packageName.contains(".domain.repository")) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(klass),
                    "Repository interface '$className' should be in domain.repository package"
                )
            )
        }
    }
}