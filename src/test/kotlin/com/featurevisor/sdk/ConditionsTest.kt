package com.featurevisor.sdk

import com.featurevisor.types.AttributeValue
import com.featurevisor.types.Condition
import com.featurevisor.types.ConditionValue
import com.featurevisor.types.Operator
import com.featurevisor.types.Operator.*
import io.kotest.matchers.shouldBe
import java.sql.Date
import java.time.LocalDate
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConditionsTest {

    val calendar = Calendar.getInstance()

    @Test
    fun `EQUALS operator works for strings`() {
        val condition =
            Condition.Plain(
                attributeKey = "browser_type",
                operator = EQUALS,
                value = ConditionValue.StringValue("chrome")
            )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("chrome"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("firefox"))
        ) shouldBe false
    }

    @Test
    fun `NOT_EQUALS operator works for strings`() {
        val condition =
            Condition.Plain(
                attributeKey = "browser_type",
                operator = NOT_EQUALS,
                value = ConditionValue.StringValue("chrome")
            )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("firefox"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("chrome"))
        ) shouldBe false
    }

    @Test
    fun `GREATER_THAN operator works for integers`() {
        val condition =
            Condition.Plain(
                attributeKey = "age",
                operator = GREATER_THAN,
                value = ConditionValue.IntValue(18)
            )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(19))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(17))
        ) shouldBe false
    }

    @Test
    fun `LESS_THAN operator works for integers`() {
        val condition =
            Condition.Plain(
                attributeKey = "age",
                operator = LESS_THAN,
                value = ConditionValue.IntValue(18)
            )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(17))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(19))
        ) shouldBe false
    }

    @Test
    fun `GREATER_THAN_OR_EQUAL operator works for integers`() {
        val condition =
            Condition.Plain(
                attributeKey = "age",
                operator = GREATER_THAN_OR_EQUALS,
                value = ConditionValue.IntValue(18)
            )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(17))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(18))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(19))
        ) shouldBe true
    }

    @Test
    fun `LESS_THAN_OR_EQUAL operator works for integers`() {
        val condition =
            Condition.Plain(
                attributeKey = "age",
                operator = LESS_THAN_OR_EQUALS,
                value = ConditionValue.IntValue(18)
            )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(17))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(18))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("age" to AttributeValue.IntValue(19))
        ) shouldBe false
    }

    @Test
    fun `CONTAINS operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = CONTAINS,
            value = ConditionValue.StringValue("hro"),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("chrome"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition.copy(value = ConditionValue.StringValue("hrk")),
            context = mapOf("browser_type" to AttributeValue.StringValue("chrome"))
        ) shouldBe false
    }

    @Test
    fun `NOT_CONTAINS operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = CONTAINS,
            value = ConditionValue.StringValue("hro"),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("chrome"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("firefox"))
        ) shouldBe false
    }

    @Test
    fun `STARTS_WITH operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = STARTS_WITH,
            value = ConditionValue.StringValue("chr"),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("chrome"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("firefox"))
        ) shouldBe false
    }

    @Test
    fun `ENDS_WITH operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = ENDS_WITH,
            value = ConditionValue.StringValue("ome"),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("chrome"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("firefox"))
        ) shouldBe false
    }

    @Test
    fun `SEMVER_EQUALS operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_EQUALS,
            value = ConditionValue.StringValue("1.2.3")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.3"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.4"))
        ) shouldBe false
    }

    @Test
    fun `SEMVER_NOT_EQUALS operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_NOT_EQUALS,
            value = ConditionValue.StringValue("1.2.3")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.3"))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.4"))
        ) shouldBe true
    }

    @Test
    fun `SEMVER_GREATER_THAN operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_GREATER_THAN,
            value = ConditionValue.StringValue("1.2.3")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.4"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.3"))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.2"))
        ) shouldBe false
    }

    @Test
    fun `SEMVER_GREATER_THAN_OR_EQUAL operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_GREATER_THAN_OR_EQUALS,
            value = ConditionValue.StringValue("1.2.3")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.4"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.3"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.2"))
        ) shouldBe false
    }

    @Test
    fun `SEMVER_LESS_THAN operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_LESS_THAN,
            value = ConditionValue.StringValue("1.2.3")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.4"))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.3"))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.2"))
        ) shouldBe true
    }

    @Test
    fun `SEMVER_LESS_THAN_OR_EQUAL operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_LESS_THAN_OR_EQUALS,
            value = ConditionValue.StringValue("1.2.3")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.4"))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.3"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.StringValue("1.2.2"))
        ) shouldBe true
    }

    @Test
    fun `BEFORE operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "date",
            operator = BEFORE,
            value = ConditionValue.DateTimeValue(Date.valueOf("2023-10-4")),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(Date.valueOf("2022-11-4")))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(Date.valueOf("2024-10-4")))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(Date.valueOf("2024-10-4")))
        ) shouldBe false
    }

    @Test
    fun `AFTER operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "date",
            operator = AFTER,
            value = ConditionValue.DateTimeValue(Date.valueOf("2023-10-4")),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(Date.valueOf("2022-10-4")))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(Date.valueOf("2022-10-4")))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(Date.valueOf("2024-10-4")))
        ) shouldBe true
    }

    @Test
    fun `IN_ARRAY operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "letter",
            operator = IN_ARRAY,
            value = ConditionValue.ArrayValue(listOf("a", "b", "c")),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("letter" to AttributeValue.StringValue("b")),
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("letter" to AttributeValue.StringValue("d")),
        ) shouldBe false
    }

    @Test
    fun `NOT_IN_ARRAY operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "letter",
            operator = NOT_IN_ARRAY,
            value = ConditionValue.ArrayValue(listOf("a", "b", "c")),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("letter" to AttributeValue.StringValue("b")),
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("letter" to AttributeValue.StringValue("d")),
        ) shouldBe true
    }

    @Test
    fun `SEMVER_EQUALS operator works when condition value is String and attribute value is Double`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_EQUALS,
            value = ConditionValue.StringValue("1.2")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.2))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.3))
        ) shouldBe false
    }

    @Test
    fun `SEMVER_NOT_EQUALS operator works when condition value is String and attribute value is Double`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_NOT_EQUALS,
            value = ConditionValue.StringValue("1.2")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.2))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.3))
        ) shouldBe true
    }

    @Test
    fun `SEMVER_GREATER_THAN operator works when condition value is String and attribute value is Double`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_GREATER_THAN,
            value = ConditionValue.StringValue("1.2")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.2))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.3))
        ) shouldBe true
    }

    @Test
    fun `SEMVER_GREATER_THAN_OR_EQUALS operator works when condition value is String and attribute value is Double`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_GREATER_THAN_OR_EQUALS,
            value = ConditionValue.StringValue("1.2")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.1))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.2))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.3))
        ) shouldBe true
    }

    @Test
    fun `SEMVER_LESS_THAN operator works when condition value is String and attribute value is Double`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_LESS_THAN,
            value = ConditionValue.StringValue("1.2")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.1))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.2))
        ) shouldBe false
    }

    @Test
    fun `SEMVER_LESS_THAN_OR_EQUALS operator works when condition value is String and attribute value is Double`() {
        val condition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_LESS_THAN_OR_EQUALS,
            value = ConditionValue.StringValue("1.2")
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.1))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.2))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("version" to AttributeValue.DoubleValue(1.3))
        ) shouldBe false
    }

    @Test
    fun `EQUALS operator works when condition value is Int and attribute value is String`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = EQUALS,
            value = ConditionValue.IntValue(1)
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("1"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("2"))
        ) shouldBe false
    }

    @Test
    fun `NOT_EQUALS operator works when condition value is Int and attribute value is String`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = NOT_EQUALS,
            value = ConditionValue.IntValue(1)
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("1"))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("2"))
        ) shouldBe true
    }

    @Test
    fun `CONTAINS operator works when condition value is Int and attribute value is String`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = CONTAINS,
            value = ConditionValue.IntValue(1)
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("123"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("23"))
        ) shouldBe false
    }

    @Test
    fun `NOT_CONTAINS operator works when condition value is Int and attribute value is String`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = NOT_CONTAINS,
            value = ConditionValue.IntValue(1)
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("123"))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("23"))
        ) shouldBe true
    }

    @Test
    fun `STARTS_WITH operator works when condition value is Int and attribute value is String`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = STARTS_WITH,
            value = ConditionValue.IntValue(1)
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("123"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("23"))
        ) shouldBe false
    }

    @Test
    fun `ENDS_WITH operator works when condition value is Int and attribute value is String`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = ENDS_WITH,
            value = ConditionValue.IntValue(3)
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("123"))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.StringValue("25"))
        ) shouldBe false
    }

    @Test
    fun `IN_ARRAY operator works when condition value is Array and attribute value is Int`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = IN_ARRAY,
            value = ConditionValue.ArrayValue(listOf("1","2","3","4"))
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.IntValue(1))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.IntValue(5))
        ) shouldBe false
    }

    @Test
    fun `NOT_IN_ARRAY operator works when condition value is Array and attribute value is Int`() {
        val condition = Condition.Plain(
            attributeKey = "browser_type",
            operator = NOT_IN_ARRAY,
            value = ConditionValue.ArrayValue(listOf("1","2","3","4"))
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.IntValue(1))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("browser_type" to AttributeValue.IntValue(5))
        ) shouldBe true
    }

    @Test
    fun `multiple conditions work`() {
        val startsWithCondition = Condition.Plain(
            attributeKey = "browser_type",
            operator = STARTS_WITH,
            value = ConditionValue.StringValue("chr"),
        )

        val semVerCondition = Condition.Plain(
            attributeKey = "version",
            operator = SEMVER_GREATER_THAN,
            value = ConditionValue.StringValue("1.2.3")
        )

        val ageCondition =
            Condition.Plain(
                attributeKey = "age",
                operator = GREATER_THAN,
                value = ConditionValue.IntValue(18)
            )

        val beforeCondition = Condition.Plain(
            attributeKey = "date",
            operator = BEFORE,
            value = ConditionValue.DateTimeValue(Date(1632307200000)),
        )

        val inArrayCondition = Condition.Plain(
            attributeKey = "letter",
            operator = IN_ARRAY,
            value = ConditionValue.ArrayValue(listOf("a", "b", "c")),
        )

        val condition = Condition.And(
            listOf(
                Condition.And(
                    listOf(
                        startsWithCondition,
                        semVerCondition,
                    )
                ),
                Condition.Or(
                    listOf(
                        ageCondition,
                        beforeCondition,
                    )
                ),
                Condition.Not(
                    listOf(
                        inArrayCondition
                    )
                )
            )
        )

        val context = mapOf(
            "browser_type" to AttributeValue.StringValue("chrome"), // true
            "version" to AttributeValue.StringValue("1.2.4"), // true
            "date" to AttributeValue.DateValue(Date.valueOf("2023-10-4")), // false
            "letter" to AttributeValue.StringValue("x"), // false
            "age" to AttributeValue.IntValue(19), // true
        )

        Conditions.allConditionsAreMatched(
            condition = condition,
            context = context,
        ) shouldBe true

        Conditions.allConditionsAreMatched(
            condition = condition,
            context = context.toMutableMap().apply {
                this["age"] = AttributeValue.IntValue(17)
            },
        ) shouldBe false
    }
}
