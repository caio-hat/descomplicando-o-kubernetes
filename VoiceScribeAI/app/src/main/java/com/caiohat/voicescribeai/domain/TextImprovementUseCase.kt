package com.caiohat.voicescribeai.domain

import com.caiohat.voicescribeai.data.ai.GeminiRepository
import com.caiohat.voicescribeai.data.ai.GemmaRepository
import com.caiohat.voicescribeai.data.preferences.AppPreferences
import com.caiohat.voicescribeai.domain.models.AIProvider
import kotlinx.coroutines.flow.first

class TextImprovementUseCase(
    private val prefs: AppPreferences,
    private val geminiRepo: GeminiRepository,
    private val gemmaRepo: GemmaRepository
) {
    suspend fun improve(rawText: String): Pair<String, AIProvider> {
        val provider = prefs.aiProvider.first()

        return when (provider) {
            AIProvider.GEMMA_LOCAL -> {
                val improved = gemmaRepo.improveText(rawText)
                Pair(improved, AIProvider.GEMMA_LOCAL)
            }
            AIProvider.GEMINI_API -> {
                val apiKey = prefs.getGeminiApiKey()
                if (apiKey.isBlank()) throw IllegalStateException("API Key não configurada")
                val improved = geminiRepo.improveText(rawText, apiKey)
                Pair(improved, AIProvider.GEMINI_API)
            }
        }
    }
}
