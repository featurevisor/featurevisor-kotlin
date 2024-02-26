package com.featurevisor.sdk.serializers

import com.featurevisor.types.Condition
import com.featurevisor.types.ConditionValue
import com.featurevisor.types.Operator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class ConditionSerializerTest {

    @Test
    fun `decode PLAIN condition`() {
        val element = """
            {
                "attribute": "version",
                "operator": "equals",
                "value": "1.2.3"
            }
        """.trimIndent()

        val condition = Json.decodeFromString<Condition>(element)

        condition.shouldBeTypeOf<Condition.Plain>()
        condition.attributeKey shouldBe "version"
        condition.operator shouldBe Operator.EQUALS
        condition.value shouldBe ConditionValue.StringValue("1.2.3")
    }

    @Test
    fun `decode AND condition`() {
        val element = """
            {
                "and": [{
                    "attribute": "version",
                    "operator": "equals",
                    "value": "1.2.3"
                }, {
                    "attribute": "age",
                    "operator": "greaterThanOrEquals",
                    "value": "18"
                }]
            }
        """.trimIndent()

        val condition = Json.decodeFromString<Condition>(element)

        condition.shouldBeTypeOf<Condition.And>()
        (condition.and[0] as Condition.Plain).run {
            attributeKey shouldBe "version"
            operator shouldBe Operator.EQUALS
            value shouldBe ConditionValue.StringValue("1.2.3")
        }
        (condition.and[1] as Condition.Plain).run {
            attributeKey shouldBe "age"
            operator shouldBe Operator.GREATER_THAN_OR_EQUALS
            value shouldBe ConditionValue.IntValue(18)
        }
    }

    @Test
    fun `decode OR condition`() {
        val element = """
            {
                "or": [{
                    "attribute": "version",
                    "operator": "equals",
                    "value": "1.2.3"
                }, {
                    "attribute": "age",
                    "operator": "greaterThanOrEquals",
                    "value": "18"
                }]
            }
        """.trimIndent()

        val condition = Json.decodeFromString<Condition>(element)

        condition.shouldBeTypeOf<Condition.Or>()
        (condition.or[0] as Condition.Plain).run {
            attributeKey shouldBe "version"
            operator shouldBe Operator.EQUALS
            value shouldBe ConditionValue.StringValue("1.2.3")
        }
        (condition.or[1] as Condition.Plain).run {
            attributeKey shouldBe "age"
            operator shouldBe Operator.GREATER_THAN_OR_EQUALS
            value shouldBe ConditionValue.IntValue(18)
        }
    }

    @Test
    fun `decode NOT condition`() {
        val element = """
            {
                "not": [{
                    "attribute": "version",
                    "operator": "equals",
                    "value": "1.2.3"
                }]
            }
        """.trimIndent()

        val condition = Json.decodeFromString<Condition>(element)

        condition.shouldBeTypeOf<Condition.Not>()
        (condition.not[0] as Condition.Plain).run {
            attributeKey shouldBe "version"
            operator shouldBe Operator.EQUALS
            value shouldBe ConditionValue.StringValue("1.2.3")
        }
    }
}
