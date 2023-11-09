package com.featurevisor.sdk

import com.featurevisor.sdk.types.EventName.ACTIVATION
import com.featurevisor.sdk.types.EventName.READY
import com.featurevisor.sdk.types.EventName.REFRESH
import com.featurevisor.sdk.types.EventName.UPDATE
import com.featurevisor.sdk.types.EventName.values
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class EmitterTest {

    private val readyCallback: Listener = mockk {
        every { this@mockk(emptyArray()) } answers { nothing }
    }
    private val refreshCallback: Listener = mockk {
        every { this@mockk(emptyArray()) } answers { nothing }
    }
    private val updateCallback: Listener = mockk {
        every { this@mockk(emptyArray()) } answers { nothing }
    }
    private val activationCallback: Listener = mockk {
        every { this@mockk(emptyArray()) } answers { nothing }
    }

    private val systemUnderTest = Emitter()

    @Test
    fun `add listeners and confirm they are invoked`() {
        systemUnderTest.addListener(READY, readyCallback)
        systemUnderTest.addListener(REFRESH, refreshCallback)
        systemUnderTest.addListener(UPDATE, updateCallback)
        systemUnderTest.addListener(ACTIVATION, activationCallback)

        values().forEach {
            systemUnderTest.emit(it)
        }

        verify(exactly = 1) {
            readyCallback(any())
            refreshCallback(any())
            updateCallback(any())
            activationCallback(any())
        }
    }

    @Test
    fun `removed listener is no longer invoked`() {
        systemUnderTest.addListener(READY, readyCallback)
        systemUnderTest.addListener(REFRESH, refreshCallback)
        systemUnderTest.addListener(UPDATE, updateCallback)

        systemUnderTest.removeListener(REFRESH)
        values().forEach {
            systemUnderTest.emit(it)
        }

        verify(exactly = 1) {
            readyCallback(any())
            updateCallback(any())
        }
        verify(exactly = 0) {
            refreshCallback(any())
        }
    }

    @Test
    fun `removeAllListeners() works correctly`() {
        systemUnderTest.addListener(READY, readyCallback)
        systemUnderTest.addListener(REFRESH, refreshCallback)
        systemUnderTest.addListener(UPDATE, updateCallback)
        systemUnderTest.addListener(ACTIVATION, activationCallback)

        systemUnderTest.removeAllListeners()
        values().forEach {
            systemUnderTest.emit(it)
        }

        verify(exactly = 0) {
            readyCallback(any())
            refreshCallback(any())
            updateCallback(any())
            activationCallback(any())
        }
    }
}
