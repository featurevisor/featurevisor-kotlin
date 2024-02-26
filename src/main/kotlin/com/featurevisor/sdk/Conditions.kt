package com.featurevisor.sdk

import com.featurevisor.types.AttributeValue
import com.featurevisor.types.Condition
import com.featurevisor.types.Condition.And
import com.featurevisor.types.Condition.Not
import com.featurevisor.types.Condition.Or
import com.featurevisor.types.Condition.Plain
import com.featurevisor.types.ConditionValue
import com.featurevisor.types.Context
import com.featurevisor.types.Operator.AFTER
import com.featurevisor.types.Operator.BEFORE
import com.featurevisor.types.Operator.CONTAINS
import com.featurevisor.types.Operator.ENDS_WITH
import com.featurevisor.types.Operator.EQUALS
import com.featurevisor.types.Operator.GREATER_THAN
import com.featurevisor.types.Operator.GREATER_THAN_OR_EQUALS
import com.featurevisor.types.Operator.IN_ARRAY
import com.featurevisor.types.Operator.LESS_THAN
import com.featurevisor.types.Operator.LESS_THAN_OR_EQUALS
import com.featurevisor.types.Operator.NOT_CONTAINS
import com.featurevisor.types.Operator.NOT_EQUALS
import com.featurevisor.types.Operator.NOT_IN_ARRAY
import com.featurevisor.types.Operator.SEMVER_EQUALS
import com.featurevisor.types.Operator.SEMVER_GREATER_THAN
import com.featurevisor.types.Operator.SEMVER_GREATER_THAN_OR_EQUALS
import com.featurevisor.types.Operator.SEMVER_LESS_THAN
import com.featurevisor.types.Operator.SEMVER_LESS_THAN_OR_EQUALS
import com.featurevisor.types.Operator.SEMVER_NOT_EQUALS
import com.featurevisor.types.Operator.STARTS_WITH
import net.swiftzer.semver.SemVer

object Conditions {

    fun conditionIsMatched(condition: Plain, context: Context): Boolean {
        val (attributeKey, operator, conditionValue) = condition
        val attributeValue = context.getOrDefault(attributeKey, null) ?: return false

        return when {
            attributeValue is AttributeValue.StringValue && conditionValue is ConditionValue.StringValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    NOT_EQUALS -> attributeValue.value != conditionValue.value
                    CONTAINS -> attributeValue.value.contains(conditionValue.value)
                    NOT_CONTAINS -> attributeValue.value.contains(conditionValue.value).not()
                    STARTS_WITH -> attributeValue.value.startsWith(conditionValue.value)
                    ENDS_WITH -> attributeValue.value.endsWith(conditionValue.value)
                    SEMVER_EQUALS -> compareVersions(
                        attributeValue.value,
                        conditionValue.value,
                    ) == 0

                    SEMVER_NOT_EQUALS -> compareVersions(
                        attributeValue.value,
                        conditionValue.value,
                    ) != 0

                    SEMVER_GREATER_THAN -> compareVersions(
                        attributeValue.value,
                        conditionValue.value
                    ) == 1

                    SEMVER_GREATER_THAN_OR_EQUALS -> compareVersions(
                        attributeValue.value,
                        conditionValue.value
                    ) >= 0

                    SEMVER_LESS_THAN -> compareVersions(
                        attributeValue.value,
                        conditionValue.value
                    ) == -1

                    SEMVER_LESS_THAN_OR_EQUALS -> compareVersions(
                        attributeValue.value,
                        conditionValue.value
                    ) <= 0

                    else -> false
                }
            }

            attributeValue is AttributeValue.IntValue && conditionValue is ConditionValue.IntValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    NOT_EQUALS -> attributeValue.value != conditionValue.value
                    GREATER_THAN -> attributeValue.value > conditionValue.value
                    GREATER_THAN_OR_EQUALS -> attributeValue.value >= conditionValue.value
                    LESS_THAN -> attributeValue.value < conditionValue.value
                    LESS_THAN_OR_EQUALS -> attributeValue.value <= conditionValue.value
                    else -> false
                }
            }

            attributeValue is AttributeValue.DoubleValue && conditionValue is ConditionValue.DoubleValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    NOT_EQUALS -> attributeValue.value != conditionValue.value
                    GREATER_THAN -> attributeValue.value > conditionValue.value
                    GREATER_THAN_OR_EQUALS -> attributeValue.value >= conditionValue.value
                    LESS_THAN -> attributeValue.value < conditionValue.value
                    LESS_THAN_OR_EQUALS -> attributeValue.value <= conditionValue.value
                    else -> false
                }
            }

            attributeValue is AttributeValue.BooleanValue && conditionValue is ConditionValue.BooleanValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    NOT_EQUALS -> attributeValue.value != conditionValue.value
                    else -> false
                }
            }

            attributeValue is AttributeValue.StringValue && conditionValue is ConditionValue.ArrayValue -> {
                when (operator) {
                    IN_ARRAY -> attributeValue.value in conditionValue.values
                    NOT_IN_ARRAY -> (attributeValue.value in conditionValue.values).not()
                    else -> false
                }
            }

            attributeValue is AttributeValue.DateValue && conditionValue is ConditionValue.DateTimeValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    BEFORE -> attributeValue.value < conditionValue.value
                    AFTER -> attributeValue.value > conditionValue.value
                    else -> false
                }
            }

            else -> false
        }
    }

    fun allConditionsAreMatched(condition: Condition, context: Context): Boolean {
        return when (condition) {
            is Plain -> conditionIsMatched(condition, context)
            is And -> condition.and.all { allConditionsAreMatched(it, context) }
            is Or -> condition.or.any { allConditionsAreMatched(it, context) }
            is Not -> condition.not.all { allConditionsAreMatched(it, context).not() }
        }
    }

    private fun compareVersions(actual: String, condition: String): Int {
        return SemVer.parse(actual).compareTo(SemVer.parse(condition))
    }
}
