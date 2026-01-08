package com.kroslabs.linkedincommunicator.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import com.kroslabs.linkedincommunicator.data.model.SyncStatus

@Composable
fun SyncStatusIcon(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "sync_rotation"
    )

    val (icon, tint, rotationModifier) = when (status) {
        SyncStatus.IDLE -> Triple(Icons.Default.Cloud, MaterialTheme.colorScheme.onSurfaceVariant, Modifier)
        SyncStatus.SYNCING -> Triple(Icons.Default.CloudSync, MaterialTheme.colorScheme.primary, Modifier.rotate(rotation))
        SyncStatus.SYNCED -> Triple(Icons.Default.CloudDone, Color(0xFF4CAF50), Modifier)
        SyncStatus.ERROR -> Triple(Icons.Default.Error, Color.Red, Modifier)
        SyncStatus.OFFLINE -> Triple(Icons.Default.CloudOff, MaterialTheme.colorScheme.onSurfaceVariant, Modifier)
    }

    Icon(
        imageVector = icon,
        contentDescription = "Sync status: ${status.name}",
        tint = tint,
        modifier = modifier.then(rotationModifier)
    )
}
