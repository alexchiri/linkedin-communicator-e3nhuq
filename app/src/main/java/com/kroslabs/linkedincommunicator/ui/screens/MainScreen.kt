package com.kroslabs.linkedincommunicator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kroslabs.linkedincommunicator.data.model.Post
import com.kroslabs.linkedincommunicator.ui.MainViewModel
import com.kroslabs.linkedincommunicator.ui.components.SyncStatusIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val currentPost by viewModel.currentPost.collectAsState()
    val savedDrafts by viewModel.savedDrafts.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val diffViewState by viewModel.diffViewState.collectAsState()
    val translationHelpState by viewModel.translationHelpState.collectAsState()
    val assembledPostState by viewModel.assembledPostState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDraftPicker by remember { mutableStateOf(false) }
    var showPostSwitcher by remember { mutableStateOf(false) }
    var showVersionHistory by remember { mutableStateOf(false) }
    var showAiActions by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LinkedIn Communicator") },
                actions = {
                    SyncStatusIcon(status = syncStatus)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Post management buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.createNewPost() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Post")
                    }
                    Button(
                        onClick = { showDraftPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load Draft")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.saveCurrentPost()
                                snackbarHostState.showSnackbar("Post saved")
                            }
                        },
                        enabled = currentPost != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }

                // Content area
                if (currentPost == null) {
                    WelcomeContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )
                } else {
                    PostEditorScreen(
                        viewModel = viewModel,
                        onShowAiActions = { showAiActions = true },
                        onShowPostSwitcher = { showPostSwitcher = true },
                        onShowVersionHistory = { showVersionHistory = true },
                        onAssemblePost = { viewModel.assemblePost() }
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Dialogs
    if (showDraftPicker) {
        DraftPickerDialog(
            drafts = savedDrafts,
            onDraftSelected = { post ->
                viewModel.openPost(post)
                showDraftPicker = false
            },
            onDeleteDraft = { post ->
                viewModel.deletePost(post)
            },
            onDismiss = { showDraftPicker = false }
        )
    }

    if (showPostSwitcher) {
        PostSwitcherDialog(
            viewModel = viewModel,
            onDismiss = { showPostSwitcher = false }
        )
    }

    if (showVersionHistory) {
        VersionHistoryDialog(
            viewModel = viewModel,
            onDismiss = { showVersionHistory = false }
        )
    }

    if (showAiActions) {
        AiActionsSheet(
            viewModel = viewModel,
            onDismiss = { showAiActions = false }
        )
    }

    if (diffViewState.isVisible) {
        DiffViewDialog(
            state = diffViewState,
            onAccept = { viewModel.acceptDiffChanges() },
            onReject = { viewModel.rejectDiffChanges() }
        )
    }

    if (translationHelpState.isVisible) {
        TranslationHelpDialog(
            state = translationHelpState,
            onSuggestionSelected = { viewModel.acceptTranslationSuggestion(it) },
            onDismiss = { viewModel.dismissTranslationHelp() }
        )
    }

    if (assembledPostState.isVisible) {
        AssembledPostDialog(
            state = assembledPostState,
            onDismiss = { viewModel.dismissAssembledPost() },
            onCopied = {
                viewModel.dismissAssembledPost()
                scope.launch {
                    snackbarHostState.showSnackbar("Post copied to clipboard")
                }
            }
        )
    }
}

@Composable
private fun WelcomeContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to LinkedIn Communicator",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Create multilingual LinkedIn posts with AI assistance.\n\n" +
                    "Tap \"New Post\" to get started or \"Load Draft\" to continue working on a saved post.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
