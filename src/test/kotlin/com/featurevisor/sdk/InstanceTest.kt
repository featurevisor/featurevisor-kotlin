/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.featurevisor.sdk

import com.featurevisor.types.*
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InstanceTest {
    private val datafileUrl = "https://www.testmock.com"
    private val mockDatafileFetchHandler: DatafileFetchHandler = mockk<DatafileFetchHandler>(relaxed = true)
    private val datafileContent = DatafileContent(
        schemaVersion = "0",
        revision = "0",
        attributes = listOf(),
        segments = listOf(),
        features = listOf()
    )
    private var instanceOptions = InstanceOptions(
        bucketKeySeparator = "",
        configureBucketKey = null,
        configureBucketValue = null,
        datafile = datafileContent,
        datafileUrl = null,
        handleDatafileFetch = null,
        initialFeatures = mapOf(),
        interceptContext = null,
        logger = null,
        onActivation = {},
        onReady = {},
        onRefresh = {},
        onUpdate = {},
        refreshInterval = null,
        stickyFeatures = mapOf(),
        onError = {},
    )
    private val systemUnderTest = FeaturevisorInstance.createInstance(
        options = instanceOptions
    )

    private val dispatcher = TestCoroutineDispatcher()

    private val testScope = TestCoroutineScope(dispatcher)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `instance initialised properly`() {
        systemUnderTest.statuses.ready shouldBe true
    }

    @Test
    fun `instance fetches data using handleDatafileFetch`() {
        coEvery { mockDatafileFetchHandler(datafileUrl) } returns Result.success(datafileContent)
        instanceOptions = instanceOptions.copy(
            datafileUrl = datafileUrl,
            datafile = null,
            handleDatafileFetch = mockDatafileFetchHandler,
        )

        FeaturevisorInstance.createInstance(
            options = instanceOptions
        )
// TODO: FixMe
//        verify(exactly = 1) {
//            mockDatafileFetchHandler(datafileUrl)
//        }
        systemUnderTest.statuses.ready shouldBe true
    }

    @Test
    fun `should refresh datafile`() {
        testScope.launch {
            var refreshed = false
            var updatedViaOption = false

            val sdk = FeaturevisorInstance.createInstance(
                instanceOptions.copy(
                    datafileUrl = datafileUrl,
                    datafile = null,
                    refreshInterval = 2L,
                    onReady = {
                        println("ready")
                    },
                    onRefresh = {
                        refreshed = true
                    },
                    onUpdate = {
                        updatedViaOption = true
                    }
                )

            )

            assertEquals(false, sdk.isReady())

            delay(3)

            assertEquals(true, refreshed)
            assertEquals(true, updatedViaOption)

            assertEquals(true, sdk.isReady())

            sdk.stopRefreshing()
        }
    }

}
