package com.featurevisor.sdk

import com.featurevisor.sdk.LogLevel.*

typealias LogMessage = String
typealias LogDetails = Map<String, Any>
typealias LogHandler = (level: LogLevel, message: LogMessage, details: LogDetails?) -> Unit

enum class LogLevel(val value: String) {
    ERROR("error"),
    WARN("warn"),
    INFO("info"),
    DEBUG("debug")
}

class Logger(
    private var levels: List<LogLevel>,
    private val handle: LogHandler,
) {

    companion object {
        private val defaultLogLevels: List<LogLevel> = listOf(ERROR, WARN)
        private val defaultLogHandler: LogHandler = { level, message, _ ->
            println("[${level.value}] $message")
        }

        fun createLogger(levels: List<LogLevel> = defaultLogLevels, handle: LogHandler = defaultLogHandler): Logger =
            Logger(levels, handle)
    }
    fun setLevels(levels: List<LogLevel>) {
        this.levels = levels
    }

    fun debug(message: LogMessage, details: LogDetails? = null) =
        log(DEBUG, message, details)

    fun info(message: LogMessage, details: LogDetails? = null) =
        log(INFO, message, details)

    fun warn(message: LogMessage, details: LogDetails? = null) =
        log(WARN, message, details)

    fun error(message: LogMessage, details: LogDetails? = null) =
        log(ERROR, message, details)


    private fun log(level: LogLevel, message: LogMessage, details: LogDetails? = null) {
        if (level in levels) {
            handle(level, message, details)
        }
    }
}
