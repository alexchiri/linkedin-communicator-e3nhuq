package com.kroslabs.linkedincommunicator.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.kroslabs.linkedincommunicator.data.api.ClaudeApiService
import com.kroslabs.linkedincommunicator.data.model.Language
import com.kroslabs.linkedincommunicator.data.model.Post
import com.kroslabs.linkedincommunicator.data.model.PostVersion
import com.kroslabs.linkedincommunicator.data.model.SyncStatus
import com.kroslabs.linkedincommunicator.data.model.WorkflowStage
import com.kroslabs.linkedincommunicator.data.storage.PostStorage
import com.kroslabs.linkedincommunicator.data.storage.PreferencesManager
import com.kroslabs.linkedincommunicator.data.storage.SecureStorage
import com.kroslabs.linkedincommunicator.data.sync.GoogleDriveSyncService
import com.kroslabs.linkedincommunicator.logging.DebugLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DiffViewState(
    val originalText: String = "",
    val modifiedText: String = "",
    val language: Language = Language.SWEDISH,
    val newStage: WorkflowStage? = null,
    val isVisible: Boolean = false
)

data class TranslationHelpState(
    val originalText: String = "",
    val alternatives: List<String> = emptyList(),
    val language: Language = Language.SWEDISH,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val isVisible: Boolean = false
)

data class AssembledPostState(
    val assembledText: String = "",
    val isVisible: Boolean = false
)

class MainViewModel(
    private val postStorage: PostStorage,
    private val secureStorage: SecureStorage,
    private val preferencesManager: PreferencesManager,
    private val syncService: GoogleDriveSyncService,
    private val apiService: ClaudeApiService
) : ViewModel() {

    private val tag = "MainViewModel"

    private val _currentPost = MutableStateFlow<Post?>(null)
    val currentPost: StateFlow<Post?> = _currentPost.asStateFlow()

    private val _openPosts = MutableStateFlow<List<Post>>(emptyList())
    val openPosts: StateFlow<List<Post>> = _openPosts.asStateFlow()

    private val _savedDrafts = MutableStateFlow<List<Post>>(emptyList())
    val savedDrafts: StateFlow<List<Post>> = _savedDrafts.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(Language.SWEDISH)
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _diffViewState = MutableStateFlow(DiffViewState())
    val diffViewState: StateFlow<DiffViewState> = _diffViewState.asStateFlow()

    private val _translationHelpState = MutableStateFlow(TranslationHelpState())
    val translationHelpState: StateFlow<TranslationHelpState> = _translationHelpState.asStateFlow()

    private val _assembledPostState = MutableStateFlow(AssembledPostState())
    val assembledPostState: StateFlow<AssembledPostState> = _assembledPostState.asStateFlow()

    val syncStatus: StateFlow<SyncStatus> = syncService.syncStatus
    val isSignedIn: StateFlow<Boolean> = syncService.isSignedIn
    val markdownMode = preferencesManager.markdownMode
    val cloudSyncEnabled = preferencesManager.cloudSyncEnabled
    val lastSyncTimestamp = preferencesManager.lastSyncTimestamp

    private var autoSaveJob: Job? = null
    private var periodicSyncJob: Job? = null

    init {
        loadSavedDrafts()
        syncService.checkSignInStatus()
        startPeriodicSync()
    }

    private fun startPeriodicSync() {
        periodicSyncJob?.cancel()
        periodicSyncJob = viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000) // 5 minutes
                if (preferencesManager.cloudSyncEnabled.first()) {
                    syncService.sync()
                    loadSavedDrafts()
                }
            }
        }
    }

    fun loadSavedDrafts() {
        viewModelScope.launch {
            _savedDrafts.value = postStorage.loadAllPosts()
        }
    }

    fun createNewPost() {
        val newPost = Post()
        _openPosts.value = _openPosts.value + newPost
        _currentPost.value = newPost
        _selectedLanguage.value = Language.SWEDISH
        startAutoSave()
        DebugLogger.d(tag, "Created new post: ${newPost.id}")
    }

    fun openPost(post: Post) {
        if (_openPosts.value.none { it.id == post.id }) {
            _openPosts.value = _openPosts.value + post
        }
        _currentPost.value = post
        _selectedLanguage.value = Language.SWEDISH
        startAutoSave()
        DebugLogger.d(tag, "Opened post: ${post.id}")
    }

    fun switchToPost(post: Post) {
        _currentPost.value = post
        DebugLogger.d(tag, "Switched to post: ${post.id}")
    }

    fun closeCurrentPost() {
        val current = _currentPost.value ?: return
        viewModelScope.launch {
            saveCurrentPost()
            _openPosts.value = _openPosts.value.filter { it.id != current.id }
            _currentPost.value = _openPosts.value.lastOrNull()
            loadSavedDrafts()
            DebugLogger.d(tag, "Closed post: ${current.id}")
        }
    }

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
    }

    fun updateText(text: String, language: Language) {
        val post = _currentPost.value ?: return
        val updatedPost = when (language) {
            Language.SWEDISH -> post.copy(swedishText = text, modifiedAt = System.currentTimeMillis())
            Language.ENGLISH -> post.copy(englishText = text, modifiedAt = System.currentTimeMillis())
            Language.ROMANIAN -> post.copy(romanianText = text, modifiedAt = System.currentTimeMillis())
        }
        _currentPost.value = updatedPost
        _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
    }

    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(30000) // 30 seconds
                saveCurrentPost()
            }
        }
    }

    suspend fun saveCurrentPost(): Boolean {
        val post = _currentPost.value ?: return false
        if (post.swedishText.isBlank() && post.englishText.isBlank() && post.romanianText.isBlank()) {
            return true // Don't save empty posts
        }
        val saved = postStorage.savePost(post)
        if (saved) {
            loadSavedDrafts()
            DebugLogger.d(tag, "Saved post: ${post.id}")
        }
        return saved
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            postStorage.deletePost(post.id)
            _openPosts.value = _openPosts.value.filter { it.id != post.id }
            if (_currentPost.value?.id == post.id) {
                _currentPost.value = _openPosts.value.lastOrNull()
            }
            loadSavedDrafts()
            DebugLogger.d(tag, "Deleted post: ${post.id}")
        }
    }

    private fun createVersionSnapshot() {
        val post = _currentPost.value ?: return
        val version = PostVersion(
            swedishText = post.swedishText,
            englishText = post.englishText,
            romanianText = post.romanianText,
            workflowStage = post.workflowStage
        )
        val newHistory = (post.versionHistory + version).takeLast(50)
        val updatedPost = post.copy(versionHistory = newHistory)
        _currentPost.value = updatedPost
        _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
    }

    fun restoreVersion(version: PostVersion) {
        val post = _currentPost.value ?: return
        createVersionSnapshot() // Save current state first
        val updatedPost = post.copy(
            swedishText = version.swedishText,
            englishText = version.englishText,
            romanianText = version.romanianText,
            workflowStage = version.workflowStage,
            modifiedAt = System.currentTimeMillis()
        )
        _currentPost.value = updatedPost
        _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
        DebugLogger.d(tag, "Restored version from ${version.timestamp}")
    }

    // AI Actions
    fun proofreadSwedish() {
        val post = _currentPost.value ?: return
        if (post.swedishText.isBlank()) {
            _errorMessage.value = "No Swedish text to proofread"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                _errorMessage.value = "API key not configured. Please add your API key in Settings."
                _isLoading.value = false
                return@launch
            }

            createVersionSnapshot()
            val useMarkdown = preferencesManager.markdownMode.first()
            when (val result = apiService.proofreadSwedish(apiKey, post.swedishText, useMarkdown)) {
                is ClaudeApiService.ApiResult.Success -> {
                    _diffViewState.value = DiffViewState(
                        originalText = post.swedishText,
                        modifiedText = result.data,
                        language = Language.SWEDISH,
                        newStage = WorkflowStage.PROOFREAD,
                        isVisible = true
                    )
                }
                is ClaudeApiService.ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun makeTextConcise() {
        val post = _currentPost.value ?: return
        if (post.swedishText.isBlank()) {
            _errorMessage.value = "No Swedish text to condense"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                _errorMessage.value = "API key not configured. Please add your API key in Settings."
                _isLoading.value = false
                return@launch
            }

            createVersionSnapshot()
            val useMarkdown = preferencesManager.markdownMode.first()
            when (val result = apiService.makeTextConcise(apiKey, post.swedishText, useMarkdown)) {
                is ClaudeApiService.ApiResult.Success -> {
                    _diffViewState.value = DiffViewState(
                        originalText = post.swedishText,
                        modifiedText = result.data,
                        language = Language.SWEDISH,
                        newStage = WorkflowStage.CONDENSED,
                        isVisible = true
                    )
                }
                is ClaudeApiService.ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun translateToEnglish() {
        val post = _currentPost.value ?: return
        if (post.swedishText.isBlank()) {
            _errorMessage.value = "No Swedish text to translate"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                _errorMessage.value = "API key not configured. Please add your API key in Settings."
                _isLoading.value = false
                return@launch
            }

            createVersionSnapshot()
            val useMarkdown = preferencesManager.markdownMode.first()
            when (val result = apiService.translateToEnglish(apiKey, post.swedishText, useMarkdown)) {
                is ClaudeApiService.ApiResult.Success -> {
                    val updatedPost = post.copy(
                        englishText = result.data,
                        workflowStage = WorkflowStage.TRANSLATED,
                        modifiedAt = System.currentTimeMillis()
                    )
                    _currentPost.value = updatedPost
                    _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
                    _selectedLanguage.value = Language.ENGLISH
                }
                is ClaudeApiService.ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun translateToRomanian() {
        val post = _currentPost.value ?: return
        if (post.swedishText.isBlank()) {
            _errorMessage.value = "No Swedish text to translate"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                _errorMessage.value = "API key not configured. Please add your API key in Settings."
                _isLoading.value = false
                return@launch
            }

            createVersionSnapshot()
            val useMarkdown = preferencesManager.markdownMode.first()
            when (val result = apiService.translateToRomanian(apiKey, post.swedishText, useMarkdown)) {
                is ClaudeApiService.ApiResult.Success -> {
                    val updatedPost = post.copy(
                        romanianText = result.data,
                        workflowStage = WorkflowStage.TRANSLATED,
                        modifiedAt = System.currentTimeMillis()
                    )
                    _currentPost.value = updatedPost
                    _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
                    _selectedLanguage.value = Language.ROMANIAN
                }
                is ClaudeApiService.ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun translateToBothLanguages() {
        val post = _currentPost.value ?: return
        if (post.swedishText.isBlank()) {
            _errorMessage.value = "No Swedish text to translate"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                _errorMessage.value = "API key not configured. Please add your API key in Settings."
                _isLoading.value = false
                return@launch
            }

            createVersionSnapshot()
            val useMarkdown = preferencesManager.markdownMode.first()

            var englishResult: String? = null
            var romanianResult: String? = null
            var error: String? = null

            // Translate to both in parallel
            val englishJob = launch {
                when (val result = apiService.translateToEnglish(apiKey, post.swedishText, useMarkdown)) {
                    is ClaudeApiService.ApiResult.Success -> englishResult = result.data
                    is ClaudeApiService.ApiResult.Error -> error = result.message
                }
            }
            val romanianJob = launch {
                when (val result = apiService.translateToRomanian(apiKey, post.swedishText, useMarkdown)) {
                    is ClaudeApiService.ApiResult.Success -> romanianResult = result.data
                    is ClaudeApiService.ApiResult.Error -> if (error == null) error = result.message
                }
            }

            englishJob.join()
            romanianJob.join()

            if (error != null) {
                _errorMessage.value = error
            } else {
                val updatedPost = post.copy(
                    englishText = englishResult ?: post.englishText,
                    romanianText = romanianResult ?: post.romanianText,
                    workflowStage = WorkflowStage.TRANSLATED,
                    modifiedAt = System.currentTimeMillis()
                )
                _currentPost.value = updatedPost
                _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
            }
            _isLoading.value = false
        }
    }

    fun translateToSwedish() {
        val post = _currentPost.value ?: return
        val currentLang = _selectedLanguage.value
        val sourceText = when (currentLang) {
            Language.ENGLISH -> post.englishText
            Language.ROMANIAN -> post.romanianText
            Language.SWEDISH -> return
        }

        if (sourceText.isBlank()) {
            _errorMessage.value = "No ${currentLang.displayName} text to translate"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                _errorMessage.value = "API key not configured. Please add your API key in Settings."
                _isLoading.value = false
                return@launch
            }

            createVersionSnapshot()
            val useMarkdown = preferencesManager.markdownMode.first()
            when (val result = apiService.translateToSwedish(apiKey, sourceText, currentLang.displayName, useMarkdown)) {
                is ClaudeApiService.ApiResult.Success -> {
                    val updatedPost = post.copy(
                        swedishText = result.data,
                        modifiedAt = System.currentTimeMillis()
                    )
                    _currentPost.value = updatedPost
                    _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
                    _selectedLanguage.value = Language.SWEDISH
                }
                is ClaudeApiService.ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun getTranslationHelp(selectedText: String, selectionStart: Int, selectionEnd: Int) {
        if (selectedText.isBlank()) {
            _errorMessage.value = "No text selected"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                _errorMessage.value = "API key not configured. Please add your API key in Settings."
                _isLoading.value = false
                return@launch
            }

            when (val result = apiService.getTranslationHelp(apiKey, selectedText, _selectedLanguage.value.displayName)) {
                is ClaudeApiService.ApiResult.Success -> {
                    _translationHelpState.value = TranslationHelpState(
                        originalText = selectedText,
                        alternatives = result.data,
                        language = _selectedLanguage.value,
                        selectionStart = selectionStart,
                        selectionEnd = selectionEnd,
                        isVisible = true
                    )
                }
                is ClaudeApiService.ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun acceptTranslationSuggestion(suggestion: String) {
        val post = _currentPost.value ?: return
        val state = _translationHelpState.value
        val currentText = when (state.language) {
            Language.SWEDISH -> post.swedishText
            Language.ENGLISH -> post.englishText
            Language.ROMANIAN -> post.romanianText
        }

        val newText = currentText.substring(0, state.selectionStart) +
                suggestion +
                currentText.substring(state.selectionEnd)

        updateText(newText, state.language)
        dismissTranslationHelp()
    }

    fun dismissTranslationHelp() {
        _translationHelpState.value = TranslationHelpState()
    }

    fun acceptDiffChanges() {
        val state = _diffViewState.value
        val post = _currentPost.value ?: return

        val updatedPost = when (state.language) {
            Language.SWEDISH -> post.copy(
                swedishText = state.modifiedText,
                workflowStage = state.newStage ?: post.workflowStage,
                modifiedAt = System.currentTimeMillis()
            )
            Language.ENGLISH -> post.copy(
                englishText = state.modifiedText,
                workflowStage = state.newStage ?: post.workflowStage,
                modifiedAt = System.currentTimeMillis()
            )
            Language.ROMANIAN -> post.copy(
                romanianText = state.modifiedText,
                workflowStage = state.newStage ?: post.workflowStage,
                modifiedAt = System.currentTimeMillis()
            )
        }

        _currentPost.value = updatedPost
        _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
        _diffViewState.value = DiffViewState()
    }

    fun rejectDiffChanges() {
        _diffViewState.value = DiffViewState()
    }

    fun updateWorkflowStage(stage: WorkflowStage) {
        val post = _currentPost.value ?: return
        val updatedPost = post.copy(workflowStage = stage, modifiedAt = System.currentTimeMillis())
        _currentPost.value = updatedPost
        _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }
    }

    fun assemblePost() {
        val post = _currentPost.value ?: return

        if (post.swedishText.isBlank() && post.englishText.isBlank() && post.romanianText.isBlank()) {
            _errorMessage.value = "No content to assemble"
            return
        }

        val assembledText = """
[🇸🇪] [🇬🇧 below] [🇷🇴 mai jos]
${post.swedishText}

[🇬🇧] [🇷🇴 mai jos]
${post.englishText}

[🇷🇴]
${post.romanianText}
        """.trimIndent()

        // Update workflow stage to Ready to Post
        val updatedPost = post.copy(
            workflowStage = WorkflowStage.READY_TO_POST,
            modifiedAt = System.currentTimeMillis()
        )
        _currentPost.value = updatedPost
        _openPosts.value = _openPosts.value.map { if (it.id == post.id) updatedPost else it }

        _assembledPostState.value = AssembledPostState(
            assembledText = assembledText,
            isVisible = true
        )
        DebugLogger.d(tag, "Assembled post and set to Ready to Post")
    }

    fun dismissAssembledPost() {
        _assembledPostState.value = AssembledPostState()
    }

    // Settings
    fun saveApiKey(apiKey: String) {
        secureStorage.saveApiKey(apiKey)
    }

    fun hasApiKey(): Boolean = secureStorage.hasApiKey()

    fun setMarkdownMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setMarkdownMode(enabled)
        }
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setCloudSyncEnabled(enabled)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncService.sync()
            loadSavedDrafts()
        }
    }

    fun handleGoogleSignIn(account: GoogleSignInAccount) {
        syncService.initializeDriveService(account)
        syncNow()
    }

    fun signOutGoogle() {
        syncService.signOut()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val postStorage = PostStorage(context)
            val secureStorage = SecureStorage(context)
            val preferencesManager = PreferencesManager(context)
            val syncService = GoogleDriveSyncService(context, postStorage, preferencesManager)
            val apiService = ClaudeApiService()
            return MainViewModel(postStorage, secureStorage, preferencesManager, syncService, apiService) as T
        }
    }
}
