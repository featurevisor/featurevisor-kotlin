package com.featurevisor.sdk

import com.featurevisor.sdk.factory.MockDatafileContentFactory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DataFileReaderTest {

    private val systemUnderTest = DataFileReader(
        datafileJson = MockDatafileContentFactory.get()
    )

    @Test
    fun `getRevision() returns correct value`() {
        systemUnderTest.getRevision() shouldBe "revision"
    }

    @Test
    fun `getSchemaVersion returns correct value`() {
        systemUnderTest.getSchemaVersion() shouldBe "schemaVersion"
    }

    @Test
    fun `getAllAttributes() returns correct list`() {
        systemUnderTest.getAllAttributes() shouldBe MockDatafileContentFactory.getAttributes()
    }

    @Test
    fun `getAttribute() returns correct value`() {
        systemUnderTest.getAttribute("browser_type") shouldBe MockDatafileContentFactory.getAttributes().first()
    }

    @Test
    fun `getSegment() returns correct value`() {
        systemUnderTest.getSegment("netherlands") shouldBe MockDatafileContentFactory.getSegments().first()
    }

    @Test
    fun `getFeature() returns correct value`() {
        systemUnderTest.getFeature("landing_page") shouldBe MockDatafileContentFactory.getFeatures().first()
    }

    @Test
    fun `return null if key not present in collection`() {
        systemUnderTest.getAttribute("country") shouldBe null
        systemUnderTest.getSegment("germany") shouldBe null
        systemUnderTest.getFeature("key_moments") shouldBe null
    }
}
