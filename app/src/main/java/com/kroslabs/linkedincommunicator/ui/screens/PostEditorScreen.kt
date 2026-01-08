package com.kroslabs.linkedincommunicator.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kroslabs.linkedincommunicator.data.model.Language
import com.kroslabs.linkedincommunicator.ui.MainViewModel
import com.kroslabs.linkedincommunicator.ui.components.CharacterCounter
import com.kroslabs.linkedincommunicator.ui.components.WorkflowBadge

@Composable
fun PostEditorScreen(
    viewModel: MainViewModel,
    onShowAiActions: () -> Unit,
    onShowPostSwitcher: () -> Unit,
    onShowVersionHistory: () -> Unit,
    onAssemblePost: () -> Unit
) {
    val post by viewModel.currentPost.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val currentPost = post ?: return

    val currentText = when (selectedLanguage) {
        Language.SWEDISH -> currentPost.swedishText
        Language.ENGLISH -> currentPost.englishText
        Language.ROMANIAN -> currentPost.romanianText
    }

    var textFieldValue by remember(currentPost.id, selectedLanguage) {
        mutableStateOf(TextFieldValue(currentText, TextRange(currentText.length)))
    }

    // Sync text when post or language changes externally (e.g., AI operations, loading different post)
    LaunchedEffect(currentPost.id, selectedLanguage, currentText) {
        if (textFieldValue.text != currentText) {
            textFieldValue = TextFieldValue(currentText, TextRange(currentText.length))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Workflow badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkflowBadge(stage = currentPost.workflowStage)
            CharacterCounter(count = currentText.length)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Language tabs
        ScrollableTabRow(
            selectedTabIndex = Language.entries.indexOf(selectedLanguage),
            modifier = Modifier.fillMaxWidth()
        ) {
            Language.entries.forEach { language ->
                Tab(
                    selected = selectedLanguage == language,
                    onClick = { viewModel.selectLanguage(language) },
                    text = { Text(language.displayName) }
                )
            }
        }

        // Text editor
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                viewModel.updateText(newValue.text, selectedLanguage)
            },
            placeholder = { Text(selectedLanguage.placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            minLines = 10,
            singleLine = false
        )

        // Bottom toolbar
        BottomAppBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToolbarButton(onClick = onShowAiActions, icon = Icons.Default.AutoAwesome, label = "AI")
                ToolbarButton(onClick = onShowPostSwitcher, icon = Icons.Default.SwapHoriz, label = "Switch")
                ToolbarButton(onClick = onShowVersionHistory, icon = Icons.Default.History, label = "History")
                ToolbarButton(onClick = onAssemblePost, icon = Icons.Default.MergeType, label = "Assemble")
                ToolbarButton(onClick = { viewModel.closeCurrentPost() }, icon = Icons.Default.Close, label = "Close")
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = label)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
