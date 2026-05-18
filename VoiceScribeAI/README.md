# VoiceScribe AI

App Android para transcrição de voz com melhoria de texto por IA.

## Funcionalidades

- **Gravação de áudio** local via microfone
- **Transcrição com Whisper** (whisper.cpp JNI) — 100% on-device
- **Melhoria de texto por IA**:
  - **Gemma 2B on-device** (via MediaPipe LLM Inference) — sem internet
  - **Gemini 2.0 Flash API** (nuvem) — requer chave de API
- Interface Material 3 em português

## Requisitos

- Android 8.0+ (API 26)
- **Snapdragon 8 Gen 1 ou superior** (arm64-v8a)
- ~200MB para modelo Whisper Base
- ~1.5GB para modelo Gemma 2B (opcional)

## Modelos suportados

### Whisper (transcrição)

| Modelo | Tamanho | Velocidade | Precisão |
|--------|---------|------------|----------|
| Tiny   | 75MB    | Muito rápido | Boa |
| **Base** | **142MB** | **Rápido** | **Muito boa** ← recomendado |
| Small  | 466MB   | Moderado | Excelente |

Os modelos são baixados automaticamente na primeira execução.

### Gemma 2B (melhoria local)

Baixe o arquivo `gemma-2b-it-gpu-int4.bin` do [Kaggle](https://www.kaggle.com/models/google/gemma/frameworks/tfLite) e copie para:

```
Android/data/com.caiohat.voicescribeai/files/models/gemma-2b-it-gpu-int4.bin
```

## Configuração

1. Abra o app → toque em **Configurações** (ícone no canto superior)
2. Escolha o provedor de IA: **Gemma Local** ou **Gemini API**
3. Se Gemini API: cole sua chave em **Chave da API Gemini**
4. Baixe o modelo Whisper desejado (Base recomendado)
5. Volte para a tela principal e comece a gravar

## Build

```bash
./gradlew assembleDebug
```

## Arquitetura

```
MVVM + Clean Architecture
├── data/
│   ├── audio/     — AudioRecord 16kHz PCM mono
│   ├── transcription/ — whisper-jni wrapper + download
│   ├── ai/        — GeminiRepository (REST) + GemmaRepository (MediaPipe)
│   └── preferences/ — EncryptedSharedPrefs + DataStore
├── domain/        — TextImprovementUseCase
├── ui/            — Jetpack Compose (Material 3)
└── viewmodel/     — MainViewModel + SettingsViewModel
```

## Dependências principais

- `io.github.givimad:whisper-jni:1.7.1` — Whisper on-device
- `com.google.mediapipe:tasks-genai:0.10.27` — Gemma on-device
- `com.squareup.retrofit2:retrofit:2.11.0` — Gemini API
- Jetpack Compose BOM 2025.01.00
