package com.featurevisor.sdk

import com.featurevisor.types.AttributeValue
import com.featurevisor.types.ConditionValue
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
                        Operator.equals,
                        ConditionValue.StringValue("chrome")
                )

        val context = mapOf("browser_type" to AttributeValue.StringValue("chrome"))

        val result = Conditions.conditionIsMatched(condition, context)

        assertEquals(true, result)
    }
}
