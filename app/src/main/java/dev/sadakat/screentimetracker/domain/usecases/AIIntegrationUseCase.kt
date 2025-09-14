package dev.sadakat.screentimetracker.domain.usecases

import javax.inject.Inject

class AIIntegrationUseCase @Inject constructor() {
    // Simplified implementation for now
    suspend fun generateInsights(): List<String> {
        return listOf("AI insights coming soon")
    }
    
    suspend fun generateRecommendations(): List<String> {
        return listOf("AI recommendations coming soon")
    }
}