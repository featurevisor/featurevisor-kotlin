package com.featurevisor.sdk

import com.featurevisor.types.AttributeValue
import com.featurevisor.types.Operator
import com.featurevisor.types.PlainCondition
import kotlin.test.Test
import kotlin.test.assertEquals

class ConditionsTest {
    @Test
    fun testEqualsOperatorForStrings() {
        val condition =
            PlainCondition(
                "browser_type",
                Operator.EQUALS,
                AttributeValue.StringValue("chrome")
            )

        // match
        assertEquals(
            true,
            Conditions.conditionIsMatched(
                condition,
                mapOf("browser_type" to AttributeValue.StringValue("chrome"))
            )
        )

        // not match
        assertEquals(
            false,
            Conditions.conditionIsMatched(
                condition,
                mapOf("browser_type" to AttributeValue.StringValue("firefox"))
            )
        )
    }

    @Test
    fun testNotEqualsOperatorForStrings() {
        val condition =
            PlainCondition(
                "browser_type",
                Operator.NOT_EQUALS,
                AttributeValue.StringValue("chrome")
            )

        // match
        assertEquals(
            true,
            Conditions.conditionIsMatched(
                condition,
                mapOf("browser_type" to AttributeValue.StringValue("firefox"))
            )
        )

        // not match
        assertEquals(
            false,
            Conditions.conditionIsMatched(
                condition,
                mapOf("browser_type" to AttributeValue.StringValue("chrome"))
            )
        )
    }

    @Test
    fun testGreaterThanOperator() {
        val condition = PlainCondition("age", Operator.GREATER_THAN, AttributeValue.IntValue(18))

        // match
        assertEquals(
            true,
            Conditions.conditionIsMatched(
                condition,
                mapOf("age" to AttributeValue.IntValue(19))
            )
        )

        // not match
        assertEquals(
            false,
            Conditions.conditionIsMatched(
                condition,
                mapOf("age" to AttributeValue.IntValue(17))
            )
        )
    }

    @Test
    fun testLessThanOperator() {
        val condition = PlainCondition("age", Operator.LESS_THAN, AttributeValue.IntValue(18))

        // match
        assertEquals(
            true,
            Conditions.conditionIsMatched(
                condition,
                mapOf("age" to AttributeValue.IntValue(17))
            )
        )

        // not match
        assertEquals(
            false,
            Conditions.conditionIsMatched(
                condition,
                mapOf("age" to AttributeValue.IntValue(19))
            )
        )
    }
}
