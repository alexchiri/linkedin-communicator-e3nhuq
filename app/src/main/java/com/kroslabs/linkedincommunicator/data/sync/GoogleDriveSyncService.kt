package com.kroslabs.linkedincommunicator.data.sync

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.kroslabs.linkedincommunicator.data.model.Post
import com.kroslabs.linkedincommunicator.data.model.SyncStatus
import com.kroslabs.linkedincommunicator.data.storage.PostStorage
import com.kroslabs.linkedincommunicator.data.storage.PreferencesManager
import com.kroslabs.linkedincommunicator.logging.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File

class GoogleDriveSyncService(
    private val context: Context,
    private val postStorage: PostStorage,
    private val preferencesManager: PreferencesManager
) {
    private val tag = "GoogleDriveSyncService"
    private val appFolderName = "LinkedInCommunicator"

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private var driveService: Drive? = null

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun checkSignInStatus() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        _isSignedIn.value = account != null
        if (account != null) {
            initializeDriveService(account)
        }
    }

    fun initializeDriveService(account: GoogleSignInAccount) {
        DebugLogger.d(tag, "Initializing Drive service for ${account.email}")
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("LinkedIn Communicator")
            .build()

        _isSignedIn.value = true
    }

    fun signOut() {
        googleSignInClient.signOut()
        driveService = null
        _isSignedIn.value = false
        DebugLogger.d(tag, "Signed out from Google Drive")
    }

    suspend fun sync(): Boolean = withContext(Dispatchers.IO) {
        val syncEnabled = preferencesManager.cloudSyncEnabled.first()
        if (!syncEnabled) {
            DebugLogger.d(tag, "Sync disabled, skipping")
            return@withContext true
        }

        val drive = driveService
        if (drive == null) {
            DebugLogger.w(tag, "Drive service not initialized")
            _syncStatus.value = SyncStatus.OFFLINE
            return@withContext false
        }

        try {
            _syncStatus.value = SyncStatus.SYNCING
            DebugLogger.d(tag, "Starting sync")

            // Get or create app folder
            val folderId = getOrCreateAppFolder(drive)

            // Upload local posts
            val localPosts = postStorage.loadAllPosts()
            for (post in localPosts) {
                uploadPost(drive, folderId, post)
            }

            // Download remote posts
            downloadRemotePosts(drive, folderId)

            preferencesManager.setLastSyncTimestamp(System.currentTimeMillis())
            _syncStatus.value = SyncStatus.SYNCED
            DebugLogger.d(tag, "Sync completed successfully")
            true
        } catch (e: Exception) {
            DebugLogger.e(tag, "Sync failed", e)
            _syncStatus.value = SyncStatus.ERROR
            false
        }
    }

    private fun getOrCreateAppFolder(drive: Drive): String {
        // Search for existing folder
        val query = "name = '$appFolderName' and mimeType = 'application/vnd.google-apps.folder' and 'appDataFolder' in parents and trashed = false"
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ(query)
            .execute()

        if (result.files.isNotEmpty()) {
            return result.files[0].id
        }

        // Create folder
        val folderMetadata = com.google.api.services.drive.model.File().apply {
            name = appFolderName
            mimeType = "application/vnd.google-apps.folder"
            parents = listOf("appDataFolder")
        }
        val folder = drive.files().create(folderMetadata)
            .setFields("id")
            .execute()
        DebugLogger.d(tag, "Created app folder: ${folder.id}")
        return folder.id
    }

    private fun uploadPost(drive: Drive, folderId: String, post: Post) {
        val fileName = "${post.id}.json"

        // Check if file exists
        val query = "name = '$fileName' and '$folderId' in parents and trashed = false"
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ(query)
            .execute()

        val jsonContent = json.encodeToString(Post.serializer(), post)
        val tempFile = File.createTempFile("post", ".json")
        tempFile.writeText(jsonContent)
        val mediaContent = FileContent("application/json", tempFile)

        if (result.files.isNotEmpty()) {
            // Check if we need to update
            val existingFileId = result.files[0].id
            val existingContent = downloadFileContent(drive, existingFileId)
            if (existingContent != null) {
                val existingPost = json.decodeFromString(Post.serializer(), existingContent)
                if (existingPost.modifiedAt >= post.modifiedAt) {
                    // Remote is newer or same, skip upload
                    tempFile.delete()
                    return
                }
            }

            // Update existing file
            drive.files().update(existingFileId, null, mediaContent).execute()
            DebugLogger.d(tag, "Updated post in Drive: ${post.id}")
        } else {
            // Create new file
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = fileName
                parents = listOf(folderId)
            }
            drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            DebugLogger.d(tag, "Uploaded new post to Drive: ${post.id}")
        }

        tempFile.delete()
    }

    private suspend fun downloadRemotePosts(drive: Drive, folderId: String) {
        val query = "name contains '.json' and '$folderId' in parents and trashed = false"
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ(query)
            .execute()

        for (file in result.files) {
            val content = downloadFileContent(drive, file.id) ?: continue
            try {
                val remotePost = json.decodeFromString(Post.serializer(), content)
                val localPost = postStorage.loadPost(remotePost.id)

                if (localPost == null || remotePost.modifiedAt > localPost.modifiedAt) {
                    postStorage.savePost(remotePost)
                    DebugLogger.d(tag, "Downloaded/updated post from Drive: ${remotePost.id}")
                }
            } catch (e: Exception) {
                DebugLogger.e(tag, "Failed to parse remote post: ${file.name}", e)
            }
        }
    }

    private fun downloadFileContent(drive: Drive, fileId: String): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.toString()
        } catch (e: Exception) {
            DebugLogger.e(tag, "Failed to download file: $fileId", e)
            null
        }
    }

    fun setOffline() {
        _syncStatus.value = SyncStatus.OFFLINE
    }

    fun setIdle() {
        _syncStatus.value = SyncStatus.IDLE
    }
}
