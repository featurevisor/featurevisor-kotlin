package com.featurevisor.sdk.serializers

import com.featurevisor.types.ConditionValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlinx.serialization.decodeFromString

class ConditionValueTest {

    @Test
    fun `decode int value with correct type`() {
        val element = """
            1
        """.trimIndent()

        val result = Json.decodeFromString<ConditionValue>(element)

        result.shouldBeTypeOf<ConditionValue.IntValue>()
        result.value shouldBe 1
    }

    @Test
    fun `decode boolean value with correct type`() {
        val element = """
            true
        """.trimIndent()

        val result = Json.decodeFromString<ConditionValue>(element)

        result.shouldBeTypeOf<ConditionValue.BooleanValue>()
        result.value shouldBe true
    }

    @Test
    fun `decode double value with correct type`() {
        val element = """
            1.2
        """.trimIndent()

        val result = Json.decodeFromString<ConditionValue>(element)

        result.shouldBeTypeOf<ConditionValue.DoubleValue>()
        result.value shouldBe 1.2
    }

    @Test
    fun `decode string value with correct type`() {
        val element = """
            test
        """.trimIndent()

        val result = Json.decodeFromString<ConditionValue>(element)

        result.shouldBeTypeOf<ConditionValue.StringValue>()
        result.value shouldBe "test"
    }

    @Test
    fun `decode array value with correct type`() {
        val element = """
            [ "test1", "test2"]
        """.trimIndent()

        val result = Json.decodeFromString<ConditionValue>(element)

        result.shouldBeTypeOf<ConditionValue.ArrayValue>()
        result.values[0] shouldBe "test1"
        result.values[1] shouldBe "test2"
    }
}
