package com.kroslabs.linkedincommunicator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onShowVersionHistory: () -> Unit
) {
    val post by viewModel.currentPost.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val currentPost = post ?: return

    val currentText = when (selectedLanguage) {
        Language.SWEDISH -> currentPost.swedishText
        Language.ENGLISH -> currentPost.englishText
        Language.ROMANIAN -> currentPost.romanianText
    }

    var textFieldValue by remember(currentPost.id, selectedLanguage, currentText) {
        mutableStateOf(TextFieldValue(currentText))
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
        SelectionContainer {
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
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }

        // Bottom toolbar
        BottomAppBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onShowAiActions) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Actions")
                        Text("AI", style = MaterialTheme.typography.labelSmall)
                    }
                }
                IconButton(onClick = onShowPostSwitcher) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Post")
                        Text("Switch", style = MaterialTheme.typography.labelSmall)
                    }
                }
                IconButton(onClick = onShowVersionHistory) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = "Version History")
                        Text("History", style = MaterialTheme.typography.labelSmall)
                    }
                }
                IconButton(onClick = { viewModel.closeCurrentPost() }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Close, contentDescription = "Close Post")
                        Text("Close", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
