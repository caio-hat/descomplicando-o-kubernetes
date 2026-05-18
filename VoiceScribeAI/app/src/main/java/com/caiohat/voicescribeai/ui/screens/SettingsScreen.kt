package com.caiohat.voicescribeai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.caiohat.voicescribeai.data.transcription.DownloadState
import com.caiohat.voicescribeai.domain.models.AIProvider
import com.caiohat.voicescribeai.domain.models.WhisperModelSize
import com.caiohat.voicescribeai.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val aiProvider by viewModel.aiProvider.collectAsState(AIProvider.GEMINI_API)
    val whisperModel by viewModel.whisperModelSize.collectAsState(WhisperModelSize.BASE)
    val isGemmaDownloaded by viewModel.isGemmaDownloaded.collectAsState(false)
    val downloadState by viewModel.downloadState.collectAsState()
    val geminiKey by viewModel.geminiApiKey.collectAsState()

    var keyText by remember { mutableStateOf(geminiKey) }
    var showKey by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkGemmaAvailability()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Provedor de IA ---
            SectionTitle("Provedor de IA")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AIProviderOption(
                        label = "Gemma 2B (local)",
                        description = "Processamento on-device. Requer download ~1.5GB.",
                        selected = aiProvider == AIProvider.GEMMA_LOCAL,
                        onSelect = { viewModel.setAiProvider(AIProvider.GEMMA_LOCAL) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    AIProviderOption(
                        label = "Gemini API (nuvem)",
                        description = "Usa a API Gemini 2.0 Flash. Requer internet e chave de API.",
                        selected = aiProvider == AIProvider.GEMINI_API,
                        onSelect = { viewModel.setAiProvider(AIProvider.GEMINI_API) }
                    )
                }
            }

            // --- Gemini API Key ---
            if (aiProvider == AIProvider.GEMINI_API) {
                SectionTitle("Chave da API Gemini")

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("API Key") },
                    placeholder = { Text("Cole sua chave aqui") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showKey)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar/ocultar chave"
                            )
                        }
                    }
                )

                Button(
                    onClick = { viewModel.saveGeminiApiKey(keyText) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar chave")
                }
            }

            // --- Modelo Gemma ---
            if (aiProvider == AIProvider.GEMMA_LOCAL) {
                SectionTitle("Modelo Gemma 2B")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGemmaDownloaded)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (isGemmaDownloaded) {
                            Text(
                                text = "Modelo Gemma disponivel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Text(
                                text = "O modelo Gemma 2B precisa ser instalado manualmente.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Baixe o arquivo gemma-2b-it-gpu-int4.bin do Kaggle e copie para: Android/data/com.caiohat.voicescribeai/files/models/",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.checkGemmaAvailability() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Verificar modelo")
                            }
                        }
                    }
                }
            }

            // --- Modelo Whisper ---
            SectionTitle("Modelo Whisper")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    WhisperModelSize.entries.forEach { size ->
                        val isDownloaded = viewModel.isWhisperModelDownloaded(size)
                        WhisperModelOption(
                            size = size,
                            selected = whisperModel == size,
                            isDownloaded = isDownloaded,
                            onSelect = { viewModel.setWhisperModelSize(size) },
                            onDownload = { viewModel.downloadWhisperModel(size) }
                        )
                        if (size != WhisperModelSize.entries.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }

            // --- Progress de download ---
            when (val ds = downloadState) {
                is DownloadState.Downloading -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Baixando: ${(ds.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ds.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is DownloadState.Done -> {
                    Text(
                        text = "Download concluído!",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is DownloadState.Error -> {
                    Text(
                        text = "Erro: ${ds.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun AIProviderOption(
    label: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun WhisperModelOption(
    size: WhisperModelSize,
    selected: Boolean,
    isDownloaded: Boolean,
    onSelect: () -> Unit,
    onDownload: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = { if (isDownloaded) onSelect() }
        )
        Text(
            text = size.displayName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
        if (!isDownloaded) {
            Button(
                onClick = onDownload,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Baixar", style = MaterialTheme.typography.labelSmall)
            }
        } else {
            Text(
                text = "OK",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
