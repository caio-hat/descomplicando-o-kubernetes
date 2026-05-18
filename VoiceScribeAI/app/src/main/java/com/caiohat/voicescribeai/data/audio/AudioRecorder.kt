package com.caiohat.voicescribeai.data.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioRecorder {

    companion object {
        const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val recordedSamples = mutableListOf<Short>()

    @SuppressLint("MissingPermission")
    fun startRecording() {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize * 2
        )
        recordedSamples.clear()
        isRecording = true
        audioRecord?.startRecording()
    }

    suspend fun stopRecording(): FloatArray = withContext(Dispatchers.IO) {
        isRecording = false
        val record = audioRecord ?: return@withContext FloatArray(0)

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val buffer = ShortArray(bufferSize)

        // Lê os dados restantes no buffer
        while (true) {
            val read = record.read(buffer, 0, buffer.size)
            if (read <= 0) break
            recordedSamples.addAll(buffer.take(read))
        }

        record.stop()
        record.release()
        audioRecord = null

        // Converte Short PCM para Float normalizado (-1.0 a 1.0)
        FloatArray(recordedSamples.size) { i ->
            recordedSamples[i] / 32768.0f
        }
    }

    fun readAmplitude(): Float {
        val record = audioRecord ?: return 0f
        if (!isRecording) return 0f

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val buffer = ShortArray(bufferSize)
        val read = record.read(buffer, 0, buffer.size)

        if (read > 0) {
            recordedSamples.addAll(buffer.take(read))
            val maxAmplitude = buffer.take(read).maxOfOrNull { Math.abs(it.toInt()) } ?: 0
            return maxAmplitude / 32768.0f
        }
        return 0f
    }

    fun release() {
        isRecording = false
        audioRecord?.release()
        audioRecord = null
    }
}
