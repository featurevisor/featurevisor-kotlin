package com.featurevisor.sdk

import com.featurevisor.sdk.Logger.LogLevel.DEBUG
import com.featurevisor.sdk.Logger.LogLevel.ERROR
import com.featurevisor.sdk.Logger.LogLevel.INFO
import com.featurevisor.sdk.Logger.LogLevel.WARN

public typealias LogDetails = Map<String, Any>
public typealias LogHandler = (level: Logger.LogLevel, message: String, details: LogDetails?) -> Unit

public class Logger(
    private var levels: List<LogLevel>,
    private val handle: LogHandler,
) {
    public companion object {
        private val defaultLogLevels: List<LogLevel> = listOf(ERROR, WARN)
        private val defaultLogHandler: LogHandler = { level, message, _ ->
            println("[${level.value}] $message")
        }

        public fun createLogger(
            levels: List<LogLevel> = defaultLogLevels,
            handle: LogHandler = defaultLogHandler,
        ): Logger {
            return Logger(levels, handle)
        }
    }

    public fun setLevels(levels: List<LogLevel>) {
        this.levels = levels
    }

    public fun debug(message: String, details: LogDetails? = null) {
        log(DEBUG, message, details)
    }

    public fun info(message: String, details: LogDetails? = null) {
        log(INFO, message, details)
    }

    public fun warn(message: String, details: LogDetails? = null) {
        log(WARN, message, details)
    }

    public fun error(message: String, details: LogDetails? = null) {
        log(ERROR, message, details)
    }

    private fun log(level: LogLevel, message: String, details: LogDetails? = null) {
        if (level in levels) {
            handle(level, message, details)
        }
    }

    public enum class LogLevel(public val value: String) {
        ERROR("error"),
        WARN("warn"),
        INFO("info"),
        DEBUG("debug"),
    }
}
