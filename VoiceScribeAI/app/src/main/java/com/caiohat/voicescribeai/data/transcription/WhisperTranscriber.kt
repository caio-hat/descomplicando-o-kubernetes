package com.caiohat.voicescribeai.data.transcription

import com.caiohat.voicescribeai.domain.models.WhisperModelSize
import io.github.givimad.whisperjni.WhisperContext
import io.github.givimad.whisperjni.WhisperJNI
import io.github.givimad.whisperjni.WhisperParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WhisperTranscriber {

    private var whisperContext: WhisperContext? = null
    private var loadedModelSize: WhisperModelSize? = null

    companion object {
        private var isLibLoaded = false

        fun loadLibrary() {
            if (!isLibLoaded) {
                WhisperJNI.loadLibrary()
                isLibLoaded = true
            }
        }
    }

    suspend fun loadModel(modelFile: File, modelSize: WhisperModelSize) = withContext(Dispatchers.Default) {
        if (loadedModelSize == modelSize && whisperContext != null) return@withContext

        whisperContext?.close()
        whisperContext = null

        loadLibrary()

        val params = WhisperJNI.WhisperContextParams()
        whisperContext = WhisperJNI.initFromFile(modelFile.absolutePath, params)
        loadedModelSize = modelSize
    }

    suspend fun transcribe(samples: FloatArray, language: String = "pt"): String =
        withContext(Dispatchers.Default) {
            val ctx = whisperContext ?: throw IllegalStateException("Modelo não carregado")

            val params = WhisperParameters.getDefaultInstance()
            params.language = language
            params.nThreads = Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
            params.printProgress = false
            params.printTimestamps = false
            params.singleSegment = false
            params.translate = false

            ctx.full(params, samples, samples.size)

            val segments = ctx.fullNSegments()
            buildString {
                for (i in 0 until segments) {
                    append(ctx.fullGetSegmentText(i).trim())
                    if (i < segments - 1) append(" ")
                }
            }.trim()
        }

    fun release() {
        whisperContext?.close()
        whisperContext = null
        loadedModelSize = null
    }
}
