package com.featurevisor.types

sealed class AttributeValue {
    data class StringValue(val value: String) : AttributeValue()
    data class IntValue(val value: Int) : AttributeValue()
    data class DoubleValue(val value: Double) : AttributeValue()
    data class BooleanValue(val value: Boolean) : AttributeValue()
    data class ArrayValue(val values: List<String>) : AttributeValue()

    // @TODO: implement Date
    object NullValue : AttributeValue()
}
