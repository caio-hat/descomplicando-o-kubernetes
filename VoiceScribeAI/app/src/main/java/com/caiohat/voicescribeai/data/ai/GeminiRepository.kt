package com.caiohat.voicescribeai.data.ai

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
private const val GEMINI_MODEL = "gemini-2.0-flash"

// --- Modelos de request/response ---

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

// --- Interface Retrofit ---

interface GeminiApiService {
    @POST("v1beta/models/$GEMINI_MODEL:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class GeminiRepository {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(GEMINI_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(GeminiApiService::class.java)

    suspend fun improveText(rawText: String, apiKey: String): String = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(rawText)
        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
        )

        val response = service.generateContent(apiKey = apiKey, request = request)
        response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()
            ?: rawText
    }

    private fun buildPrompt(rawText: String): String = """
        Melhore o seguinte texto transcrito de voz em português brasileiro.
        Remova gaguejos, palavras repetidas, pausas ("ãh", "eh", "né", "tipo", "assim"),
        erros gramaticais e palavras fora de contexto.
        Mantenha o significado original e o estilo informal se necessário.
        Retorne APENAS o texto corrigido, sem explicações ou comentários.

        Texto:
        $rawText
    """.trimIndent()
}
