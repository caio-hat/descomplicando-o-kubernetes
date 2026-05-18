package com.caiohat.voicescribeai.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.caiohat.voicescribeai.data.audio.AudioRecorder
import com.caiohat.voicescribeai.data.preferences.AppPreferences
import com.caiohat.voicescribeai.data.transcription.ModelDownloader
import com.caiohat.voicescribeai.data.transcription.WhisperTranscriber
import com.caiohat.voicescribeai.domain.TextImprovementUseCase
import com.caiohat.voicescribeai.domain.models.TranscriptionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Transcribing : RecordingState()
    object Improving : RecordingState()
    data class Done(val result: TranscriptionResult) : RecordingState()
    data class Error(val message: String) : RecordingState()
}

class MainViewModel(
    private val context: Context,
    private val prefs: AppPreferences,
    private val modelDownloader: ModelDownloader,
    private val transcriber: WhisperTranscriber,
    private val improvementUseCase: TextImprovementUseCase
) : ViewModel() {

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    private val audioRecorder = AudioRecorder()
    private var amplitudeJob: Job? = null

    fun startRecording() {
        if (_recordingState.value is RecordingState.Recording) return

        audioRecorder.startRecording()
        _recordingState.value = RecordingState.Recording
        _amplitudes.value = emptyList()

        amplitudeJob = viewModelScope.launch {
            while (_recordingState.value is RecordingState.Recording) {
                val amp = audioRecorder.readAmplitude()
                val current = _amplitudes.value.toMutableList()
                current.add(amp)
                if (current.size > 60) current.removeAt(0)
                _amplitudes.value = current
                delay(50)
            }
        }
    }

    fun stopRecording() {
        if (_recordingState.value !is RecordingState.Recording) return

        amplitudeJob?.cancel()
        viewModelScope.launch {
            try {
                _recordingState.value = RecordingState.Transcribing
                val samples = audioRecorder.stopRecording()

                if (samples.isEmpty()) {
                    _recordingState.value = RecordingState.Error("Nenhum áudio gravado")
                    return@launch
                }

                // Carrega o modelo Whisper se necessário
                val modelSize = prefs.whisperModelSize.first()
                val modelFile = modelDownloader.getModelFile(modelSize)
                if (!modelFile.exists()) {
                    _recordingState.value = RecordingState.Error("Modelo Whisper não encontrado. Baixe nas configurações.")
                    return@launch
                }

                transcriber.loadModel(modelFile, modelSize)
                val rawText = transcriber.transcribe(samples)

                if (rawText.isBlank()) {
                    _recordingState.value = RecordingState.Error("Não foi possível transcrever o áudio")
                    return@launch
                }

                _recordingState.value = RecordingState.Improving

                val (improvedText, provider) = improvementUseCase.improve(rawText)
                _recordingState.value = RecordingState.Done(
                    TranscriptionResult(
                        rawText = rawText,
                        improvedText = improvedText,
                        provider = provider
                    )
                )

            } catch (e: IllegalStateException) {
                _recordingState.value = RecordingState.Error(e.message ?: "Erro de configuração")
            } catch (e: Exception) {
                _recordingState.value = RecordingState.Error(e.message ?: "Erro inesperado")
            }
        }
    }

    fun reset() {
        _recordingState.value = RecordingState.Idle
        _amplitudes.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.release()
        transcriber.release()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appContext = context.applicationContext
            val prefs = AppPreferences(appContext)
            val modelDownloader = ModelDownloader(appContext)
            val transcriber = WhisperTranscriber()
            val geminiRepo = com.caiohat.voicescribeai.data.ai.GeminiRepository()
            val gemmaRepo = com.caiohat.voicescribeai.data.ai.GemmaRepository(
                modelDownloader.getGemmaModelPath()
            )
            val useCase = TextImprovementUseCase(prefs, geminiRepo, gemmaRepo)
            return MainViewModel(appContext, prefs, modelDownloader, transcriber, useCase) as T
        }
    }
}
