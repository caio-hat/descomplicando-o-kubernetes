package com.caiohat.voicescribeai

import android.app.Application
import com.caiohat.voicescribeai.data.transcription.WhisperTranscriber

class VoiceScribeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WhisperTranscriber.loadLibrary()
    }
}
