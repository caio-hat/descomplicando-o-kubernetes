# whisper-jni
-keep class io.github.givimad.whisperjni.** { *; }

# MediaPipe
-keep class com.google.mediapipe.** { *; }

# Retrofit + Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.caiohat.voicescribeai.data.ai.Gemini** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
