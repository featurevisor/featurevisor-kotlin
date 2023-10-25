package com.featurevisor.sdk

import com.featurevisor.sdk.Logger.LogLevel.DEBUG
import com.featurevisor.sdk.Logger.LogLevel.ERROR
import com.featurevisor.sdk.Logger.LogLevel.INFO
import com.featurevisor.sdk.Logger.LogLevel.WARN

typealias LogDetails = Map<String, Any>
typealias LogHandler = (level: Logger.LogLevel, message: String, details: LogDetails?) -> Unit

class Logger(
    private var levels: List<LogLevel>,
    private val handle: LogHandler,
) {
    companion object {
        private val defaultLogLevels: List<LogLevel> = listOf(ERROR, WARN)
        private val defaultLogHandler: LogHandler = { level, message, _ ->
            println("[${level.value}] $message")
        }

        fun createLogger(
            levels: List<LogLevel> = defaultLogLevels,
            handle: LogHandler = defaultLogHandler,
        ) {
            Logger(levels, handle)
        }
    }

    fun setLevels(levels: List<LogLevel>) {
        this.levels = levels
    }

    fun debug(message: String, details: LogDetails? = null) {
        log(DEBUG, message, details)
    }

    fun info(message: String, details: LogDetails? = null) {
        log(INFO, message, details)
    }

    fun warn(message: String, details: LogDetails? = null) {
        log(WARN, message, details)
    }

    fun error(message: String, details: LogDetails? = null) {
        log(ERROR, message, details)
    }

    private fun log(level: LogLevel, message: String, details: LogDetails? = null) {
        if (level in levels) {
            handle(level, message, details)
        }
    }

    enum class LogLevel(val value: String) {
        ERROR("error"),
        WARN("warn"),
        INFO("info"),
        DEBUG("debug"),
    }
}
