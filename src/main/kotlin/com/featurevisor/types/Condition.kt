package com.featurevisor.types

import java.time.LocalDate

sealed class Condition {
    data class Plain(
        val attributeKey: AttributeKey,
        val operator: Operator,
        val value: ConditionValue,
    ) : Condition()

    data class And(val and: List<Condition>) : Condition()
    data class Or(val or: List<Condition>) : Condition()
    data class Not(val not: List<Condition>) : Condition()
}

sealed class ConditionValue {
    data class StringValue(val value: String) : ConditionValue()
    data class IntValue(val value: Int) : ConditionValue()
    data class DoubleValue(val value: Double) : ConditionValue()
    data class BooleanValue(val value: Boolean) : ConditionValue()
    data class ArrayValue(val values: List<String>) : ConditionValue()
    data class DateTimeValue(val value: LocalDate) : ConditionValue()
    object NullValue : ConditionValue()
}
