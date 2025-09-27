package com.usagetracker.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CleanArchRuleSetProvider : RuleSetProvider {
    override val ruleSetId = "clean-architecture"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            CleanArchitectureRule(config),
            DomainLayerPurityRule(config),
            UseCaseNamingRule(config),
            RepositoryInterfaceRule(config)
        )
    )
}