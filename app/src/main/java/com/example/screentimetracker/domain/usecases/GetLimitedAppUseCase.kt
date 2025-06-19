package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLimitedAppUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    operator fun invoke(packageName: String): Flow<LimitedApp?> {
        return repository.getLimitedApp(packageName)
    }
}
