package com.caiohat.voicescribeai.data.transcription

import android.content.Context
import com.caiohat.voicescribeai.domain.models.WhisperModelSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    data class Done(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class ModelDownloader(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    private val modelsDir: File
        get() = File(context.filesDir, "models").also { it.mkdirs() }

    // Repositório oficial ggerganov/whisper.cpp no Hugging Face
    private fun whisperModelUrl(size: WhisperModelSize): String =
        "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/${size.fileName}"

    fun getModelFile(size: WhisperModelSize): File = File(modelsDir, size.fileName)

    fun isModelDownloaded(size: WhisperModelSize): Boolean = getModelFile(size).exists()

    fun downloadWhisperModel(size: WhisperModelSize): Flow<DownloadState> = flow {
        val file = getModelFile(size)
        if (file.exists()) {
            emit(DownloadState.Done(file))
            return@flow
        }

        emit(DownloadState.Downloading(0f))

        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(whisperModelUrl(size)).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    emit(DownloadState.Error("HTTP ${response.code}"))
                    return@withContext
                }

                val body = response.body ?: run {
                    emit(DownloadState.Error("Resposta vazia"))
                    return@withContext
                }

                val totalBytes = body.contentLength()
                val tempFile = File(modelsDir, "${size.fileName}.tmp")

                body.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead = 0L
                        var len: Int

                        while (input.read(buffer).also { len = it } != -1) {
                            output.write(buffer, 0, len)
                            bytesRead += len
                            if (totalBytes > 0) {
                                emit(DownloadState.Downloading(bytesRead.toFloat() / totalBytes))
                            }
                        }
                    }
                }

                tempFile.renameTo(file)
                emit(DownloadState.Done(file))

            } catch (e: Exception) {
                emit(DownloadState.Error(e.message ?: "Erro desconhecido"))
            }
        }
    }

    // Retorna o caminho para o modelo Gemma (gerenciado externamente pelo usuário/MediaPipe)
    fun getGemmaModelPath(): String = File(modelsDir, "gemma-2b-it-gpu-int4.bin").absolutePath

    fun isGemmaModelAvailable(): Boolean = File(getGemmaModelPath()).exists()
}
