package com.caiohat.voicescribeai.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caiohat.voicescribeai.ui.components.ImprovedTextCard
import com.caiohat.voicescribeai.ui.components.RecordButton
import com.caiohat.voicescribeai.ui.components.TranscriptionCard
import com.caiohat.voicescribeai.ui.components.WaveformVisualizer
import com.caiohat.voicescribeai.viewmodel.MainViewModel
import com.caiohat.voicescribeai.viewmodel.RecordingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.recordingState.collectAsState()
    val amplitudes by viewModel.amplitudes.collectAsState()

    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        val permission = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        hasPermission = permission == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "VoiceScribe AI",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            WaveformVisualizer(
                amplitudes = amplitudes,
                isRecording = state is RecordingState.Recording,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (val s = state) {
                is RecordingState.Idle -> {
                    StatusText("Toque para gravar")
                }
                is RecordingState.Recording -> {
                    StatusText("Gravando… toque para parar")
                }
                is RecordingState.Transcribing -> {
                    CircularProgressIndicator()
                    StatusText("Transcrevendo com Whisper…")
                }
                is RecordingState.Improving -> {
                    CircularProgressIndicator()
                    StatusText("Melhorando texto com IA…")
                }
                is RecordingState.Done -> {
                    TranscriptionCard(
                        title = "Transcrição",
                        text = s.result.rawText,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (s.result.improvedText.isNotBlank()) {
                        ImprovedTextCard(
                            text = s.result.improvedText,
                            provider = s.result.provider,
                            onCopy = {
                                copyToClipboard(context, s.result.improvedText)
                            },
                            onShare = {
                                shareText(context, s.result.improvedText)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is RecordingState.Error -> {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!hasPermission) {
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Conceder permissão de microfone")
                }
            } else {
                RecordButton(
                    isRecording = state is RecordingState.Recording,
                    isEnabled = state is RecordingState.Idle ||
                            state is RecordingState.Recording ||
                            state is RecordingState.Done ||
                            state is RecordingState.Error,
                    onClick = {
                        when (state) {
                            is RecordingState.Recording -> viewModel.stopRecording()
                            else -> {
                                viewModel.reset()
                                viewModel.startRecording()
                            }
                        }
                    }
                )

                if (state is RecordingState.Done || state is RecordingState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toque no microfone para gravar novamente",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("texto melhorado", text))
    Toast.makeText(context, "Texto copiado!", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar texto"))
}
