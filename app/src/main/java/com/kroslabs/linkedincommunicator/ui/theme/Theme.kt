package com.kroslabs.linkedincommunicator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LinkedInBlue = Color(0xFF0A66C2)
private val LinkedInDarkBlue = Color(0xFF004182)
private val LinkedInLightBlue = Color(0xFFCAE5FF)

private val DarkColorScheme = darkColorScheme(
    primary = LinkedInBlue,
    secondary = LinkedInDarkBlue,
    tertiary = LinkedInLightBlue
)

private val LightColorScheme = lightColorScheme(
    primary = LinkedInBlue,
    secondary = LinkedInDarkBlue,
    tertiary = LinkedInLightBlue
)

@Composable
fun LinkedInCommunicatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
