package dev.sadakat.screentimetracker.shared.di

import dev.sadakat.screentimetracker.shared.domain.usecase.GetDashboardDataUseCase
import dev.sadakat.screentimetracker.shared.presentation.dashboard.DashboardViewModel
import dev.sadakat.screentimetracker.shared.presentation.analytics.AnalyticsViewModel
import dev.sadakat.screentimetracker.shared.presentation.goals.GoalsViewModel
import dev.sadakat.screentimetracker.shared.presentation.settings.SettingsViewModel
import dev.sadakat.screentimetracker.shared.presentation.wellness.WellnessViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val commonModule = module {
    // Use cases
    factoryOf(::GetDashboardDataUseCase)

    // ViewModels
    factory { (coroutineScope: CoroutineScope) ->
        DashboardViewModel(
            getDashboardDataUseCase = get(),
            coroutineScope = coroutineScope
        )
    }

    factory { (coroutineScope: CoroutineScope) ->
        AnalyticsViewModel(coroutineScope = coroutineScope)
    }

    factory { (coroutineScope: CoroutineScope) ->
        GoalsViewModel(coroutineScope = coroutineScope)
    }

    factory { (coroutineScope: CoroutineScope) ->
        SettingsViewModel(coroutineScope = coroutineScope)
    }

    factory { (coroutineScope: CoroutineScope) ->
        WellnessViewModel(coroutineScope = coroutineScope)
    }
}