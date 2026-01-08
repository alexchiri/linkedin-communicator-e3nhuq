package com.kroslabs.linkedincommunicator.data.storage

import android.content.Context
import com.kroslabs.linkedincommunicator.data.model.Post
import com.kroslabs.linkedincommunicator.logging.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PostStorage(private val context: Context) {
    private val tag = "PostStorage"
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val postsDir: File
        get() = File(context.filesDir, "posts").also { it.mkdirs() }

    suspend fun savePost(post: Post): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(postsDir, "${post.id}.json")
            val jsonString = json.encodeToString(post)
            file.writeText(jsonString)
            DebugLogger.d(tag, "Saved post: ${post.id}")
            true
        } catch (e: Exception) {
            DebugLogger.e(tag, "Failed to save post: ${post.id}", e)
            false
        }
    }

    suspend fun loadPost(id: String): Post? = withContext(Dispatchers.IO) {
        try {
            val file = File(postsDir, "$id.json")
            if (!file.exists()) return@withContext null
            val jsonString = file.readText()
            json.decodeFromString<Post>(jsonString)
        } catch (e: Exception) {
            DebugLogger.e(tag, "Failed to load post: $id", e)
            null
        }
    }

    suspend fun loadAllPosts(): List<Post> = withContext(Dispatchers.IO) {
        try {
            postsDir.listFiles()?.filter { it.extension == "json" }?.mapNotNull { file ->
                try {
                    json.decodeFromString<Post>(file.readText())
                } catch (e: Exception) {
                    DebugLogger.e(tag, "Failed to parse post: ${file.name}", e)
                    null
                }
            }?.sortedByDescending { it.modifiedAt } ?: emptyList()
        } catch (e: Exception) {
            DebugLogger.e(tag, "Failed to load all posts", e)
            emptyList()
        }
    }

    suspend fun deletePost(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(postsDir, "$id.json")
            val result = file.delete()
            DebugLogger.d(tag, "Deleted post: $id, success: $result")
            result
        } catch (e: Exception) {
            DebugLogger.e(tag, "Failed to delete post: $id", e)
            false
        }
    }

    fun getAllPostFiles(): List<File> {
        return postsDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()
    }
}
