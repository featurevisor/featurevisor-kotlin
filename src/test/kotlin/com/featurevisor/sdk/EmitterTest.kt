package com.featurevisor.sdk

import com.featurevisor.types.EventName.ACTIVATION
import com.featurevisor.types.EventName.READY
import com.featurevisor.types.EventName.REFRESH
import com.featurevisor.types.EventName.UPDATE
import com.featurevisor.types.EventName.values
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class EmitterTest {

    private val readyCallback: () -> Unit = mockk {
        every { this@mockk() } answers { nothing }
    }
    private val refreshCallback: () -> Unit = mockk {
        every { this@mockk() } answers { nothing }
    }
    private val updateCallback: () -> Unit = mockk {
        every { this@mockk() } answers { nothing }
    }
    private val activationCallback: () -> Unit = mockk {
        every { this@mockk() } answers { nothing }
    }

    private val systemUnderTest = Emitter()

    @Test
    fun `add listeners and confirm they are invoked`() {
        systemUnderTest.addListener(READY, readyCallback)
        systemUnderTest.addListener(REFRESH, refreshCallback)
        systemUnderTest.addListener(UPDATE, updateCallback)
        systemUnderTest.addListener(ACTIVATION, activationCallback)

        values().forEach {
            systemUnderTest.invoke(it)
        }

        verify(exactly = 1) {
            readyCallback()
            refreshCallback()
            updateCallback()
            activationCallback()
        }
    }

    @Test
    fun `removed listener is no longer invoked`() {
        systemUnderTest.addListener(READY, readyCallback)
        systemUnderTest.addListener(REFRESH, refreshCallback)
        systemUnderTest.addListener(UPDATE, updateCallback)

        systemUnderTest.removeListener(REFRESH)
        values().forEach {
            systemUnderTest.invoke(it)
        }

        verify(exactly = 1) {
            readyCallback()
            updateCallback()
        }
        verify(exactly = 0) {
            refreshCallback()
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
            systemUnderTest.invoke(it)
        }

        verify(exactly = 0) {
            readyCallback()
            refreshCallback()
            updateCallback()
            activationCallback()
        }
    }
}
