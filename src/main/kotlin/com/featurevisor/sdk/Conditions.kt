package com.featurevisor.sdk

import com.featurevisor.types.AttributeValue
import com.featurevisor.types.ConditionValue
import com.featurevisor.types.Context
import com.featurevisor.types.Operator
import com.featurevisor.types.PlainCondition

object Conditions {
    fun conditionIsMatched(condition: PlainCondition, context: Context): Boolean {
        val (attribute, operator, value) = condition

        val contextValue = context[attribute]
        val conditionValue = value

        // string
        if (contextValue is AttributeValue.StringValue) {
            // string / string
            if (conditionValue is ConditionValue.StringValue) {
                when (operator) {
                    Operator.equals -> return contextValue.value == conditionValue.value
                    Operator.notEquals -> return contextValue.value != conditionValue.value
                    Operator.contains -> return contextValue.value.contains(conditionValue.value)
                    Operator.notContains ->
                        return !contextValue.value.contains(conditionValue.value)

                    Operator.startsWith ->
                        return contextValue.value.startsWith(conditionValue.value)

                    Operator.endsWith -> return contextValue.value.endsWith(conditionValue.value)
                    else -> return false
                }
            }

            // @TODO: string / array of strings for in/notIn operators

            // @TODO: handle semvers

            return false
        }

        // int
        if (contextValue is AttributeValue.IntValue && value is ConditionValue.IntValue) {
            // int / int
            when (operator) {
                Operator.equals -> return contextValue.value == value.value
                Operator.notEquals -> return contextValue.value != value.value
                Operator.greaterThan -> return contextValue.value > value.value
                Operator.greaterThanOrEquals -> return contextValue.value >= value.value
                Operator.lessThan -> return contextValue.value < value.value
                Operator.lessThanOrEquals -> return contextValue.value <= value.value
                else -> return false
            }
        }

        // double
        if (contextValue is AttributeValue.DoubleValue && value is ConditionValue.DoubleValue) {
            // double / double
            when (operator) {
                Operator.equals -> return contextValue.value == value.value
                Operator.notEquals -> return contextValue.value != value.value
                Operator.greaterThan -> return contextValue.value > value.value
                Operator.greaterThanOrEquals -> return contextValue.value >= value.value
                Operator.lessThan -> return contextValue.value < value.value
                Operator.lessThanOrEquals -> return contextValue.value <= value.value
                else -> return false
            }
        }

        // boolean
        if (contextValue is AttributeValue.BooleanValue && value is ConditionValue.BooleanValue) {
            // boolean / boolean
            when (operator) {
                Operator.equals -> return contextValue.value == value.value
                Operator.notEquals -> return contextValue.value != value.value
                else -> return false
            }
        }

        // @TODO: handle dates

        return false
    }
}
