package com.kroslabs.linkedincommunicator.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Post(
    val id: String = UUID.randomUUID().toString(),
    val swedishText: String = "",
    val englishText: String = "",
    val romanianText: String = "",
    val workflowStage: WorkflowStage = WorkflowStage.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val versionHistory: List<PostVersion> = emptyList()
)

@Serializable
data class PostVersion(
    val timestamp: Long = System.currentTimeMillis(),
    val swedishText: String = "",
    val englishText: String = "",
    val romanianText: String = "",
    val workflowStage: WorkflowStage = WorkflowStage.DRAFT
)

@Serializable
enum class WorkflowStage {
    DRAFT,
    PROOFREAD,
    CONDENSED,
    TRANSLATED,
    REVIEWED,
    READY_TO_POST;

    val displayName: String
        get() = when (this) {
            DRAFT -> "Draft"
            PROOFREAD -> "Proofread"
            CONDENSED -> "Condensed"
            TRANSLATED -> "Translated"
            REVIEWED -> "Reviewed"
            READY_TO_POST -> "Ready to Post"
        }
}

enum class Language {
    SWEDISH,
    ENGLISH,
    ROMANIAN;

    val displayName: String
        get() = when (this) {
            SWEDISH -> "Swedish"
            ENGLISH -> "English"
            ROMANIAN -> "Romanian"
        }

    val placeholder: String
        get() = "Write your post in ${displayName}..."
}
