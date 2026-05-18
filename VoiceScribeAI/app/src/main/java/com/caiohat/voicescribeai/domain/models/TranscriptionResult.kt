package com.caiohat.voicescribeai.domain.models

data class TranscriptionResult(
    val rawText: String,
    val improvedText: String = "",
    val durationMs: Long = 0L,
    val provider: AIProvider? = null
)
