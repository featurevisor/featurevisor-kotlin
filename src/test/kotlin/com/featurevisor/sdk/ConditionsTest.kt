package com.featurevisor.sdk

import com.featurevisor.sdk.types.AttributeValue
import com.featurevisor.sdk.types.Condition
import com.featurevisor.sdk.types.ConditionValue
import com.featurevisor.sdk.types.Operator.AFTER
import com.featurevisor.sdk.types.Operator.BEFORE
import com.featurevisor.sdk.types.Operator.CONTAINS
import com.featurevisor.sdk.types.Operator.ENDS_WITH
import com.featurevisor.sdk.types.Operator.EQUALS
import com.featurevisor.sdk.types.Operator.GREATER_THAN
import com.featurevisor.sdk.types.Operator.GREATER_THAN_OR_EQUAL
import com.featurevisor.sdk.types.Operator.IN_ARRAY
import com.featurevisor.sdk.types.Operator.LESS_THAN
import com.featurevisor.sdk.types.Operator.LESS_THAN_OR_EQUAL
import com.featurevisor.sdk.types.Operator.NOT_EQUALS
import com.featurevisor.sdk.types.Operator.NOT_IN_ARRAY
import com.featurevisor.sdk.types.Operator.SEMVER_EQUALS
import com.featurevisor.sdk.types.Operator.SEMVER_GREATER_THAN
import com.featurevisor.sdk.types.Operator.SEMVER_GREATER_THAN_OR_EQUAL
import com.featurevisor.sdk.types.Operator.SEMVER_LESS_THAN
import com.featurevisor.sdk.types.Operator.SEMVER_LESS_THAN_OR_EQUAL
import com.featurevisor.sdk.types.Operator.SEMVER_NOT_EQUALS
import com.featurevisor.sdk.types.Operator.STARTS_WITH
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import kotlin.test.Test

class ConditionsTest {
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
                operator = GREATER_THAN_OR_EQUAL,
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
                operator = LESS_THAN_OR_EQUAL,
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
            operator = SEMVER_GREATER_THAN_OR_EQUAL,
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
            operator = SEMVER_LESS_THAN_OR_EQUAL,
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
            value = ConditionValue.DateTimeValue(LocalDate.of(2023, 10, 5)),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(LocalDate.of(2023, 10, 4)))
        ) shouldBe true

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(LocalDate.of(2023, 10, 5)))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(LocalDate.of(2023, 10, 6)))
        ) shouldBe false
    }

    @Test
    fun `AFTER operator works for strings`() {
        val condition = Condition.Plain(
            attributeKey = "date",
            operator = AFTER,
            value = ConditionValue.DateTimeValue(LocalDate.of(2023, 10, 5)),
        )

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(LocalDate.of(2023, 10, 4)))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(LocalDate.of(2023, 10, 5)))
        ) shouldBe false

        Conditions.conditionIsMatched(
            condition = condition,
            context = mapOf("date" to AttributeValue.DateValue(LocalDate.of(2023, 10, 6)))
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
            value = ConditionValue.DateTimeValue(LocalDate.of(2023, 10, 5)),
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
            "date" to AttributeValue.DateValue(LocalDate.of(2023, 10, 6)), // false
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
