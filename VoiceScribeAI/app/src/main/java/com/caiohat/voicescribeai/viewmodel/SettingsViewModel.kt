package com.caiohat.voicescribeai.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.caiohat.voicescribeai.data.preferences.AppPreferences
import com.caiohat.voicescribeai.data.transcription.DownloadState
import com.caiohat.voicescribeai.data.transcription.ModelDownloader
import com.caiohat.voicescribeai.domain.models.AIProvider
import com.caiohat.voicescribeai.domain.models.WhisperModelSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: AppPreferences,
    private val modelDownloader: ModelDownloader
) : ViewModel() {

    val aiProvider = prefs.aiProvider
    val whisperModelSize = prefs.whisperModelSize
    val isGemmaDownloaded = prefs.isGemmaDownloaded

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(prefs.getGeminiApiKey())
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    fun setAiProvider(provider: AIProvider) = viewModelScope.launch {
        prefs.setAiProvider(provider)
    }

    fun setWhisperModelSize(size: WhisperModelSize) = viewModelScope.launch {
        prefs.setWhisperModelSize(size)
    }

    fun saveGeminiApiKey(key: String) {
        prefs.setGeminiApiKey(key)
        _geminiApiKey.value = key
    }

    fun isWhisperModelDownloaded(size: WhisperModelSize): Boolean =
        modelDownloader.isModelDownloaded(size)

    fun downloadWhisperModel(size: WhisperModelSize) = viewModelScope.launch {
        modelDownloader.downloadWhisperModel(size).collect { state ->
            _downloadState.value = state
        }
    }

    fun downloadGemmaModel() = viewModelScope.launch {
        // Gemma requer download externo via Kaggle — orientamos o usuário
        // O arquivo deve ser copiado para: filesDir/models/gemma-2b-it-gpu-int4.bin
        if (modelDownloader.isGemmaModelAvailable()) {
            prefs.setGemmaDownloaded(true)
            _downloadState.value = DownloadState.Done(
                java.io.File(modelDownloader.getGemmaModelPath())
            )
        }
    }

    fun checkGemmaAvailability() = viewModelScope.launch {
        if (modelDownloader.isGemmaModelAvailable()) {
            prefs.setGemmaDownloaded(true)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appContext = context.applicationContext
            val prefs = AppPreferences(appContext)
            val downloader = ModelDownloader(appContext)
            return SettingsViewModel(prefs, downloader) as T
        }
    }
}
