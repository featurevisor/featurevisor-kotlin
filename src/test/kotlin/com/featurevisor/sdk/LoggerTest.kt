package com.featurevisor.sdk

import com.featurevisor.sdk.LogLevel.DEBUG
import com.featurevisor.sdk.LogLevel.ERROR
import com.featurevisor.sdk.LogLevel.INFO
import com.featurevisor.sdk.LogLevel.WARN
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class LoggerTest {

    private val mockLogMessage = "test message"
    private val mockLogDetails: Map<String, Any> = emptyMap()
    private val mockLogHandler: LogHandler = mockk {
        every { this@mockk(any(), any(), any()) } answers { nothing }
    }

    private val systemUnderTest = Logger.createLogger(
        handle = mockLogHandler,
    )

    @Test
    fun `log DEBUG message when level DEBUG is set`() {
        systemUnderTest.setLevels(listOf(DEBUG))

        systemUnderTest.debug(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly = 1) {
            mockLogHandler(DEBUG, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `log INFO message when level INFO is set`() {
        systemUnderTest.setLevels(listOf(INFO))

        systemUnderTest.info(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly = 1) {
            mockLogHandler(INFO, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `log WARN message when level WARN is set`() {
        systemUnderTest.setLevels(listOf(WARN))

        systemUnderTest.warn(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly = 1) {
            mockLogHandler(WARN, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `log ERROR message when level ERROR is set`() {
        systemUnderTest.setLevels(listOf(ERROR))

        systemUnderTest.error(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly = 1) {
            mockLogHandler(ERROR, mockLogMessage, mockLogDetails)
        }
    }

    @Test
    fun `do not log any message when not set in log levels`() {
        systemUnderTest.setLevels(listOf())

        systemUnderTest.info(
            message = mockLogMessage,
            details = mockLogDetails,
        )
        systemUnderTest.warn(
            message = mockLogMessage,
            details = mockLogDetails,
        )
        systemUnderTest.debug(
            message = mockLogMessage,
            details = mockLogDetails,
        )
        systemUnderTest.error(
            message = mockLogMessage,
            details = mockLogDetails,
        )

        verify(exactly = 0) {
            mockLogHandler(INFO, any(), any())
            mockLogHandler(WARN, any(), any())
            mockLogHandler(DEBUG, any(), any())
            mockLogHandler(ERROR, any(), any())
        }
    }
}
