package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import javax.inject.Inject

class UpdateLimitedAppUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(limitedApp: LimitedApp) {
        repository.insertLimitedApp(limitedApp) // insert with REPLACE strategy acts as update
    }
}
