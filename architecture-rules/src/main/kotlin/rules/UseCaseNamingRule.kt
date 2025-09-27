package com.usagetracker.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtClass

class UseCaseNamingRule(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = "UseCaseNaming",
        severity = Severity.Style,
        description = "Use cases should follow naming convention",
        debt = Debt.FIVE_MINS
    )

    private val useCasePattern = Regex("^[A-Z][a-zA-Z]*UseCase$")

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        val file = klass.containingKtFile
        val packageName = file.packageFqName.asString()

        // Check if we're in domain usecase package
        if ((!packageName.contains("dev.sadakat.screentimetracker.domain") &&
             !packageName.contains(".domain.")) ||
            !packageName.contains("usecase")) {
            return
        }

        val className = klass.name ?: return

        if (!useCasePattern.matches(className)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(klass),
                    "Use case '$className' should end with 'UseCase' and follow PascalCase"
                )
            )
        }
    }
}