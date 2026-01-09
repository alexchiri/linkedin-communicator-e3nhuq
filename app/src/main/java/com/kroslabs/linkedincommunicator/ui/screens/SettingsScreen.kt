package com.kroslabs.linkedincommunicator.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.kroslabs.linkedincommunicator.BuildConfig
import com.kroslabs.linkedincommunicator.data.model.SyncStatus
import com.kroslabs.linkedincommunicator.ui.MainViewModel
import com.kroslabs.linkedincommunicator.ui.components.SyncStatusIcon
import com.google.android.gms.common.api.ApiException
import com.kroslabs.linkedincommunicator.logging.DebugLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDebugLogs: () -> Unit
) {
    val context = LocalContext.current
    val markdownMode by viewModel.markdownMode.collectAsState(initial = false)
    val cloudSyncEnabled by viewModel.cloudSyncEnabled.collectAsState(initial = true)
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState(initial = 0L)
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()

    var apiKey by remember { mutableStateOf("") }
    var hasApiKey by remember { mutableStateOf(viewModel.hasApiKey()) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        DebugLogger.d("SettingsScreen", "=== Google Sign-In Result ===")
        DebugLogger.d("SettingsScreen", "Result code: ${result.resultCode} (RESULT_OK=${Activity.RESULT_OK}, RESULT_CANCELED=${Activity.RESULT_CANCELED})")

        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                DebugLogger.d("SettingsScreen", "Sign-in successful for: ${account.email}")
                DebugLogger.d("SettingsScreen", "Account ID: ${account.id}")
                DebugLogger.d("SettingsScreen", "Granted scopes: ${account.grantedScopes}")
                viewModel.handleGoogleSignIn(account)
            } catch (e: ApiException) {
                DebugLogger.e("SettingsScreen", "Sign-in failed with ApiException")
                DebugLogger.e("SettingsScreen", "Status code: ${e.statusCode}")
                DebugLogger.e("SettingsScreen", "Status message: ${e.message}")
                DebugLogger.e("SettingsScreen", "Common causes: 10 = DEVELOPER_ERROR (OAuth client not configured), 12500 = SIGN_IN_CANCELLED, 12501 = SIGN_IN_CURRENTLY_IN_PROGRESS")
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            DebugLogger.w("SettingsScreen", "Sign-in was canceled by user or failed")
            DebugLogger.w("SettingsScreen", "If you didn't cancel, check: 1) OAuth client configured in Google Cloud Console, 2) SHA-1 fingerprint added, 3) Drive API enabled")
        } else {
            DebugLogger.e("SettingsScreen", "Sign-in returned unexpected result code: ${result.resultCode}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // API Configuration
            SectionTitle("API Configuration")
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status: ")
                        if (hasApiKey) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Configured", color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Not configured", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Claude API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.saveApiKey(apiKey)
                            hasApiKey = true
                            apiKey = ""
                        },
                        enabled = apiKey.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (hasApiKey) "Update API Key" else "Save API Key")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content Preferences
            SectionTitle("Content Preferences")
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Markdown Mode")
                            Text(
                                text = if (markdownMode) "AI can use markdown formatting"
                                else "AI returns plain text only",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = markdownMode,
                            onCheckedChange = { viewModel.setMarkdownMode(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cloud Sync
            SectionTitle("Cloud Sync")
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Drive Sync")
                            Text(
                                text = "Sync posts across devices",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = cloudSyncEnabled,
                            onCheckedChange = { viewModel.setCloudSyncEnabled(it) }
                        )
                    }

                    if (cloudSyncEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status: ")
                            SyncStatusIcon(status = syncStatus)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (syncStatus) {
                                    SyncStatus.IDLE -> "Ready"
                                    SyncStatus.SYNCING -> "Syncing..."
                                    SyncStatus.SYNCED -> "Synced"
                                    SyncStatus.ERROR -> "Error"
                                    SyncStatus.OFFLINE -> "Offline"
                                }
                            )
                        }

                        if (lastSyncTimestamp > 0) {
                            val dateFormat = remember {
                                SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            }
                            Text(
                                text = "Last sync: ${dateFormat.format(Date(lastSyncTimestamp))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isSignedIn) {
                            Row {
                                Button(
                                    onClick = { viewModel.syncNow() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sync Now")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.signOutGoogle() }
                                ) {
                                    Text("Sign Out")
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    DebugLogger.d("SettingsScreen", "=== Initiating Google Sign-In ===")
                                    DebugLogger.d("SettingsScreen", "Building GoogleSignInOptions with DRIVE_APPDATA scope")
                                    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                        com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                                    )
                                        .requestEmail()
                                        .requestScopes(com.google.android.gms.common.api.Scope(com.google.api.services.drive.DriveScopes.DRIVE_APPDATA))
                                        .build()
                                    DebugLogger.d("SettingsScreen", "GoogleSignInOptions: scopes=${gso.scopeArray.map { it.scopeUri }}")

                                    val signInClient = GoogleSignIn.getClient(context, gso)
                                    DebugLogger.d("SettingsScreen", "Launching sign-in intent...")
                                    signInLauncher.launch(signInClient.signInIntent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sign in with Google")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Debug
            SectionTitle("Debug")
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = onNavigateToDebugLogs,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Debug Logs")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Information
            SectionTitle("App Information")
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Version", BuildConfig.VERSION_NAME)
                    InfoRow("Build", BuildConfig.VERSION_CODE.toString())
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
