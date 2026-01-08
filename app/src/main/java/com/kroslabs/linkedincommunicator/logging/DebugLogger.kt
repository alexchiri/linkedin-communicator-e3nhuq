package com.kroslabs.linkedincommunicator.logging

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))

    val formattedMessage: String
        get() = buildString {
            append("[$formattedTime] ${level.name}/$tag: $message")
            throwable?.let { append("\n${it.stackTraceToString()}") }
        }
}

enum class LogLevel { VERBOSE, DEBUG, INFO, WARN, ERROR }

object DebugLogger {
    private const val MAX_LOGS = 1000

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private fun addLog(entry: LogEntry) {
        _logs.value = (_logs.value + entry).takeLast(MAX_LOGS)
    }

    fun v(tag: String, message: String, throwable: Throwable? = null) {
        Log.v(tag, message, throwable)
        addLog(LogEntry(level = LogLevel.VERBOSE, tag = tag, message = message, throwable = throwable))
    }

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        Log.d(tag, message, throwable)
        addLog(LogEntry(level = LogLevel.DEBUG, tag = tag, message = message, throwable = throwable))
    }

    fun i(tag: String, message: String, throwable: Throwable? = null) {
        Log.i(tag, message, throwable)
        addLog(LogEntry(level = LogLevel.INFO, tag = tag, message = message, throwable = throwable))
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        addLog(LogEntry(level = LogLevel.WARN, tag = tag, message = message, throwable = throwable))
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        addLog(LogEntry(level = LogLevel.ERROR, tag = tag, message = message, throwable = throwable))
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun exportLogs(): String {
        return _logs.value.joinToString("\n") { it.formattedMessage }
    }
}
