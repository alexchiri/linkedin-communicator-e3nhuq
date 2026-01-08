package com.kroslabs.linkedincommunicator.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CharacterCounter(
    count: Int,
    modifier: Modifier = Modifier
) {
    val maxChars = 3000
    val warningThreshold = 2700

    val color = when {
        count > maxChars -> Color.Red
        count > warningThreshold -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val fontWeight = if (count > warningThreshold) FontWeight.Bold else FontWeight.Normal

    Text(
        text = "$count/$maxChars",
        color = color,
        fontWeight = fontWeight,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
    )
}
