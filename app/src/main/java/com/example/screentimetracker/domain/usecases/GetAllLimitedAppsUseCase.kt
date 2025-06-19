package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLimitedAppsUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    operator fun invoke(): Flow<List<LimitedApp>> {
        return repository.getAllLimitedApps()
    }
}
