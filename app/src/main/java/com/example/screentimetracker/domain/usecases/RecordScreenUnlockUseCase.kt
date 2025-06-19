package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.ScreenUnlockEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import javax.inject.Inject

class RecordScreenUnlockUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke() {
        val event = ScreenUnlockEvent(timestamp = System.currentTimeMillis())
        repository.insertScreenUnlockEvent(event)
    }
}
