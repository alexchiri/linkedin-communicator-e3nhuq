package com.kroslabs.linkedincommunicator.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kroslabs.linkedincommunicator.data.model.Language
import com.kroslabs.linkedincommunicator.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiActionsSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "AI Actions",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedLanguage) {
                Language.SWEDISH -> {
                    AiActionItem(
                        icon = Icons.Default.AutoFixHigh,
                        title = "Proofread Swedish Text",
                        description = "Check grammar, spelling, and style",
                        onClick = {
                            viewModel.proofreadSwedish()
                            onDismiss()
                        }
                    )
                    AiActionItem(
                        icon = Icons.Default.Compress,
                        title = "Make Text Concise",
                        description = "Shorten while preserving meaning",
                        onClick = {
                            viewModel.makeTextConcise()
                            onDismiss()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    AiActionItem(
                        icon = Icons.Default.Translate,
                        title = "Translate to English",
                        description = "Translate Swedish text to English",
                        onClick = {
                            viewModel.translateToEnglish()
                            onDismiss()
                        }
                    )
                    AiActionItem(
                        icon = Icons.Default.Translate,
                        title = "Translate to Romanian",
                        description = "Translate Swedish text to Romanian",
                        onClick = {
                            viewModel.translateToRomanian()
                            onDismiss()
                        }
                    )
                    AiActionItem(
                        icon = Icons.Default.Translate,
                        title = "Translate to Both Languages",
                        description = "Translate to English and Romanian",
                        onClick = {
                            viewModel.translateToBothLanguages()
                            onDismiss()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    AiActionItem(
                        icon = Icons.Default.Help,
                        title = "Get Translation Help",
                        description = "Get alternative phrasings for selected text",
                        onClick = {
                            // This requires text selection - show message
                            onDismiss()
                        }
                    )
                }
                Language.ENGLISH -> {
                    AiActionItem(
                        icon = Icons.Default.Translate,
                        title = "Translate to Swedish",
                        description = "Translate English text to Swedish",
                        onClick = {
                            viewModel.translateToSwedish()
                            onDismiss()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    AiActionItem(
                        icon = Icons.Default.Help,
                        title = "Get Translation Help",
                        description = "Get alternative phrasings for selected text",
                        onClick = {
                            onDismiss()
                        }
                    )
                }
                Language.ROMANIAN -> {
                    AiActionItem(
                        icon = Icons.Default.Translate,
                        title = "Translate to Swedish",
                        description = "Translate Romanian text to Swedish",
                        onClick = {
                            viewModel.translateToSwedish()
                            onDismiss()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    AiActionItem(
                        icon = Icons.Default.Help,
                        title = "Get Translation Help",
                        description = "Get alternative phrasings for selected text",
                        onClick = {
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AiActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
