package com.caiohat.voicescribeai.data.ai

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GemmaRepository(private val modelPath: String) {

    private var llmInference: LlmInference? = null
    private var session: LlmInferenceSession? = null

    fun initialize(context: android.content.Context) {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(1024)
            .setTopK(40)
            .setTemperature(0.7f)
            .setRandomSeed(42)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    suspend fun improveText(rawText: String): String = withContext(Dispatchers.Default) {
        val inference = llmInference
            ?: throw IllegalStateException("GemmaRepository não inicializado")

        val prompt = buildGemmaPrompt(rawText)

        suspendCancellableCoroutine { continuation ->
            val session = inference.createSession(
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(40)
                    .setTemperature(0.7f)
                    .build()
            )

            session.addQueryChunk(prompt)

            val result = StringBuilder()
            session.generateResponseAsync { partialResult, done ->
                result.append(partialResult)
                if (done) {
                    session.close()
                    continuation.resume(result.toString().trim())
                }
            }

            continuation.invokeOnCancellation {
                try { session.close() } catch (_: Exception) {}
            }
        }
    }

    private fun buildGemmaPrompt(rawText: String): String = """<start_of_turn>user
Melhore o seguinte texto transcrito de voz em português brasileiro.
Remova gaguejos, palavras repetidas, pausas ("ãh", "eh", "né", "tipo", "assim"),
erros gramaticais e palavras fora de contexto.
Mantenha o significado original e o estilo informal se necessário.
Retorne APENAS o texto corrigido, sem explicações ou comentários.

Texto:
$rawText<end_of_turn>
<start_of_turn>model
"""

    fun release() {
        session?.close()
        session = null
        llmInference?.close()
        llmInference = null
    }
}
