package com.caiohat.voicescribeai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        if (amplitudes.isEmpty()) {
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2f
            )
            return@Canvas
        }

        val barWidth = (width / amplitudes.size).coerceAtLeast(2f)
        val gap = (barWidth * 0.2f).coerceAtLeast(1f)

        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth + barWidth / 2f
            val barHeight = (amplitude * height * 0.8f).coerceAtLeast(4f)
            val alpha = if (isRecording) 1f else 0.5f

            drawLine(
                color = color.copy(alpha = alpha),
                start = Offset(x, centerY - barHeight / 2f),
                end = Offset(x, centerY + barHeight / 2f),
                strokeWidth = barWidth - gap
            )
        }
    }
}
