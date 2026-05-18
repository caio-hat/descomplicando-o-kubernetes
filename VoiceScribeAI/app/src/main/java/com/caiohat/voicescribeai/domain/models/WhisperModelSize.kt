package com.caiohat.voicescribeai.domain.models

enum class WhisperModelSize(
    val fileName: String,
    val displayName: String,
    val sizeBytes: Long
) {
    TINY("ggml-tiny.bin", "Tiny (75MB)", 75_000_000L),
    BASE("ggml-base.bin", "Base (142MB)", 142_000_000L),
    SMALL("ggml-small.bin", "Small (466MB)", 466_000_000L)
}
