package com.kroslabs.linkedincommunicator.data.api

import com.kroslabs.linkedincommunicator.logging.DebugLogger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeRequest(
    val model: String = "claude-sonnet-4-5-20250929",
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    val messages: List<ClaudeMessage>
)

@Serializable
data class ClaudeContentBlock(
    val type: String,
    val text: String? = null
)

@Serializable
data class ClaudeResponse(
    val id: String? = null,
    val content: List<ClaudeContentBlock>? = null,
    val error: ClaudeError? = null
)

@Serializable
data class ClaudeError(
    val type: String? = null,
    val message: String? = null
)

class ClaudeApiService {
    private val tag = "ClaudeApiService"
    private val baseUrl = "https://api.anthropic.com/v1/messages"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    sealed class ApiResult<out T> {
        data class Success<T>(val data: T) : ApiResult<T>()
        data class Error(val message: String) : ApiResult<Nothing>()
    }

    suspend fun sendMessage(apiKey: String, systemPrompt: String, userMessage: String): ApiResult<String> {
        return try {
            DebugLogger.d(tag, "Sending message to Claude API")
            val response = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                setBody(ClaudeRequest(
                    messages = listOf(
                        ClaudeMessage(role = "user", content = "$systemPrompt\n\n$userMessage")
                    )
                ))
            }

            val claudeResponse = response.body<ClaudeResponse>()

            if (claudeResponse.error != null) {
                DebugLogger.e(tag, "Claude API error: ${claudeResponse.error.message}")
                ApiResult.Error(claudeResponse.error.message ?: "Unknown API error")
            } else {
                val text = claudeResponse.content?.firstOrNull { it.type == "text" }?.text
                if (text != null) {
                    DebugLogger.d(tag, "Received response from Claude API")
                    ApiResult.Success(text)
                } else {
                    ApiResult.Error("No text response from API")
                }
            }
        } catch (e: Exception) {
            DebugLogger.e(tag, "Failed to call Claude API", e)
            val errorMessage = when {
                e.message?.contains("401") == true -> "API key not configured or invalid"
                e.message?.contains("429") == true -> "API rate limit exceeded - please wait and try again"
                e.message?.contains("timeout", ignoreCase = true) == true -> "Request timed out - please try again"
                e.message?.contains("Unable to resolve host") == true -> "Network error - check your connection"
                else -> e.message ?: "Unknown error"
            }
            ApiResult.Error(errorMessage)
        }
    }

    suspend fun proofreadSwedish(apiKey: String, text: String, useMarkdown: Boolean): ApiResult<String> {
        val format = if (useMarkdown) "You may use markdown formatting." else "Return plain text only without any markdown formatting."
        val systemPrompt = """You are a professional Swedish language editor. Your task is to proofread the following Swedish text for grammar, spelling, style, and professional tone suitable for LinkedIn posts.

Make corrections and improvements while preserving the original meaning and voice.
$format

Return only the corrected text, nothing else."""

        return sendMessage(apiKey, systemPrompt, text)
    }

    suspend fun makeTextConcise(apiKey: String, text: String, useMarkdown: Boolean): ApiResult<String> {
        val format = if (useMarkdown) "You may use markdown formatting." else "Return plain text only without any markdown formatting."
        val systemPrompt = """You are a professional content editor. Your task is to make the following Swedish text more concise while preserving its core meaning and professional tone for LinkedIn.

The LinkedIn character limit is 3000. Aim to reduce the text length while keeping all important information.
$format

Return only the shortened text, nothing else."""

        return sendMessage(apiKey, systemPrompt, text)
    }

    suspend fun translateToEnglish(apiKey: String, swedishText: String, useMarkdown: Boolean): ApiResult<String> {
        val format = if (useMarkdown) "You may use markdown formatting." else "Return plain text only without any markdown formatting."
        val systemPrompt = """You are a professional translator specializing in Swedish to English translation for business and professional content.

Translate the following Swedish text to English, maintaining a professional tone suitable for LinkedIn.
$format

Return only the English translation, nothing else."""

        return sendMessage(apiKey, systemPrompt, swedishText)
    }

    suspend fun translateToRomanian(apiKey: String, swedishText: String, useMarkdown: Boolean): ApiResult<String> {
        val format = if (useMarkdown) "You may use markdown formatting." else "Return plain text only without any markdown formatting."
        val systemPrompt = """You are a professional translator specializing in Swedish to Romanian translation for business and professional content.

Translate the following Swedish text to Romanian, maintaining a professional tone suitable for LinkedIn.
$format

Return only the Romanian translation, nothing else."""

        return sendMessage(apiKey, systemPrompt, swedishText)
    }

    suspend fun translateToSwedish(apiKey: String, text: String, sourceLanguage: String, useMarkdown: Boolean): ApiResult<String> {
        val format = if (useMarkdown) "You may use markdown formatting." else "Return plain text only without any markdown formatting."
        val systemPrompt = """You are a professional translator specializing in $sourceLanguage to Swedish translation for business and professional content.

Translate the following $sourceLanguage text to Swedish, maintaining a professional tone suitable for LinkedIn.
$format

Return only the Swedish translation, nothing else."""

        return sendMessage(apiKey, systemPrompt, text)
    }

    suspend fun getTranslationHelp(apiKey: String, text: String, sourceLanguage: String): ApiResult<List<String>> {
        val systemPrompt = """You are a professional translator and language expert. Provide 3-5 alternative translations or phrasings for the given text.

The source language is $sourceLanguage. Provide alternatives that maintain the professional tone suitable for LinkedIn.

Return only the alternatives, one per line, numbered 1-5. Do not include any other text or explanation."""

        return when (val result = sendMessage(apiKey, systemPrompt, text)) {
            is ApiResult.Success -> {
                val alternatives = result.data.lines()
                    .filter { it.isNotBlank() }
                    .map { it.replace(Regex("^\\d+\\.?\\s*"), "").trim() }
                    .filter { it.isNotBlank() }
                ApiResult.Success(alternatives)
            }
            is ApiResult.Error -> result
        }
    }
}
