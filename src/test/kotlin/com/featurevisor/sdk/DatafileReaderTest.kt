package com.featurevisor.sdk

import com.featurevisor.sdk.factory.DatafileContentFactory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DatafileReaderTest {

    private val systemUnderTest = DatafileReader(
        datafileJson = DatafileContentFactory.get()
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
        systemUnderTest.getAllAttributes() shouldBe DatafileContentFactory.getAttributes()
    }

    @Test
    fun `getAttribute() returns correct value`() {
        systemUnderTest.getAttribute("browser_type") shouldBe DatafileContentFactory.getAttributes().first()
    }

    @Test
    fun `getSegment() returns correct value`() {
        systemUnderTest.getSegment("netherlands") shouldBe DatafileContentFactory.getSegments().first()
    }

    @Test
    fun `getFeature() returns correct value`() {
        systemUnderTest.getFeature("landing_page") shouldBe DatafileContentFactory.getFeatures().first()
    }

    @Test
    fun `return null if key not present in collection`() {
        systemUnderTest.getAttribute("country") shouldBe null
        systemUnderTest.getSegment("germany") shouldBe null
        systemUnderTest.getFeature("key_moments") shouldBe null
    }
}
