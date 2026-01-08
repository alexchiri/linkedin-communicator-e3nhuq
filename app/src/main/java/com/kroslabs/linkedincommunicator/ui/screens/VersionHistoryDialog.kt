package com.kroslabs.linkedincommunicator.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kroslabs.linkedincommunicator.data.model.PostVersion
import com.kroslabs.linkedincommunicator.ui.MainViewModel
import com.kroslabs.linkedincommunicator.ui.components.WorkflowBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VersionHistoryDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val post by viewModel.currentPost.collectAsState()
    val versionHistory = post?.versionHistory ?: emptyList()

    var selectedVersion by remember { mutableStateOf<PostVersion?>(null) }

    if (selectedVersion != null) {
        VersionDetailDialog(
            version = selectedVersion!!,
            onRestore = {
                viewModel.restoreVersion(selectedVersion!!)
                selectedVersion = null
                onDismiss()
            },
            onDismiss = { selectedVersion = null }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Version History") },
            text = {
                if (versionHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No version history",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(versionHistory.reversed()) { version ->
                            VersionItem(
                                version = version,
                                onClick = { selectedVersion = version }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun VersionItem(
    version: PostVersion,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = dateFormat.format(Date(version.timestamp)),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            WorkflowBadge(stage = version.workflowStage)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = version.swedishText.take(100).ifBlank { "(Empty)" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VersionDetailDialog(
    version: PostVersion,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Version Details") },
        text = {
            Column {
                Text(
                    text = dateFormat.format(Date(version.timestamp)),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                WorkflowBadge(stage = version.workflowStage)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Swedish:", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = version.swedishText.ifBlank { "(Empty)" },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.height(60.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("English:", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = version.englishText.ifBlank { "(Empty)" },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.height(60.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Romanian:", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = version.romanianText.ifBlank { "(Empty)" },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.height(60.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onRestore) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
