package com.featurevisor.types

import java.time.LocalDate

typealias AttributeKey = String

data class Attribute(
    val key: AttributeKey,
    val type: String,
    val archived: Boolean?,
    val capture: Boolean?,
)

sealed class AttributeValue {
    data class StringValue(val value: String) : AttributeValue()
    data class IntValue(val value: Int) : AttributeValue()
    data class DoubleValue(val value: Double) : AttributeValue()
    data class BooleanValue(val value: Boolean) : AttributeValue()
    data class DateValue(val value: LocalDate) : AttributeValue()
    object NullValue : AttributeValue()
}
