package com.featurevisor.sdk

import com.featurevisor.types.AttributeValue
import com.featurevisor.types.AttributeValue.BooleanValue
import com.featurevisor.types.AttributeValue.DoubleValue
import com.featurevisor.types.AttributeValue.IntValue
import com.featurevisor.types.AttributeValue.StringValue
import com.featurevisor.types.Condition
import com.featurevisor.types.Condition.And
import com.featurevisor.types.Condition.Multiple
import com.featurevisor.types.Condition.Not
import com.featurevisor.types.Condition.Or
import com.featurevisor.types.Condition.Plain
import com.featurevisor.types.Context
import com.featurevisor.types.Operator.CONTAINS
import com.featurevisor.types.Operator.ENDS_WITH
import com.featurevisor.types.Operator.EQUALS
import com.featurevisor.types.Operator.GREATER_THAN
import com.featurevisor.types.Operator.GREATER_THAN_OR_EQUAL
import com.featurevisor.types.Operator.IN_ARRAY
import com.featurevisor.types.Operator.LESS_THAN
import com.featurevisor.types.Operator.LESS_THAN_OR_EQUAL
import com.featurevisor.types.Operator.NOT_CONTAINS
import com.featurevisor.types.Operator.NOT_EQUALS
import com.featurevisor.types.Operator.NOT_IN_ARRAY
import com.featurevisor.types.Operator.SEMVER_EQUALS
import com.featurevisor.types.Operator.SEMVER_GREATER_THAN
import com.featurevisor.types.Operator.SEMVER_GREATER_THAN_OR_EQUAL
import com.featurevisor.types.Operator.SEMVER_LESS_THAN
import com.featurevisor.types.Operator.SEMVER_LESS_THAN_OR_EQUAL
import com.featurevisor.types.Operator.SEMVER_NOT_EQUALS
import com.featurevisor.types.Operator.STARTS_WITH
import com.featurevisor.types.PlainCondition
import net.swiftzer.semver.SemVer

object Conditions {
    fun conditionIsMatched(condition: PlainCondition, context: Context): Boolean {
        val (attributeKey, operator, conditionValue) = condition
        val attributeValue = context.getOrDefault(attributeKey, null) ?: return false

        return when {
            attributeValue is StringValue && conditionValue is StringValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    NOT_EQUALS -> attributeValue.value != conditionValue.value
                    CONTAINS -> attributeValue.value.contains(conditionValue.value)
                    NOT_CONTAINS ->
                        !attributeValue.value.contains(conditionValue.value)

                    STARTS_WITH ->
                        attributeValue.value.startsWith(conditionValue.value)

                    ENDS_WITH -> attributeValue.value.endsWith(conditionValue.value)
                    SEMVER_EQUALS -> compareVersions(attributeValue.value, conditionValue.value) == 0
                    SEMVER_NOT_EQUALS -> compareVersions(attributeValue.value, conditionValue.value) != 0
                    SEMVER_GREATER_THAN -> compareVersions(attributeValue.value, conditionValue.value) == 1
                    SEMVER_GREATER_THAN_OR_EQUAL -> compareVersions(attributeValue.value, conditionValue.value) >= 0
                    SEMVER_LESS_THAN -> compareVersions(attributeValue.value, conditionValue.value) == -1
                    SEMVER_LESS_THAN_OR_EQUAL -> compareVersions(attributeValue.value, conditionValue.value) <= 0
                    else -> false
                }

                // @TODO: handle semvers
            }

            attributeValue is IntValue && conditionValue is IntValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    NOT_EQUALS -> attributeValue.value != conditionValue.value
                    GREATER_THAN -> attributeValue.value > conditionValue.value
                    GREATER_THAN_OR_EQUAL -> attributeValue.value >= conditionValue.value
                    LESS_THAN -> attributeValue.value < conditionValue.value
                    LESS_THAN_OR_EQUAL -> attributeValue.value <= conditionValue.value
                    else -> false
                }
            }

            attributeValue is DoubleValue && conditionValue is DoubleValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    NOT_EQUALS -> attributeValue.value != conditionValue.value
                    GREATER_THAN -> attributeValue.value > conditionValue.value
                    GREATER_THAN_OR_EQUAL -> attributeValue.value >= conditionValue.value
                    LESS_THAN -> attributeValue.value < conditionValue.value
                    LESS_THAN_OR_EQUAL -> attributeValue.value <= conditionValue.value
                    else -> false
                }
            }

            attributeValue is BooleanValue && conditionValue is BooleanValue -> {
                when (operator) {
                    EQUALS -> attributeValue.value == conditionValue.value
                    NOT_EQUALS -> attributeValue.value != conditionValue.value
                    else -> false
                }
            }

            attributeValue is StringValue && conditionValue is AttributeValue.ArrayValue -> {
                when (operator) {
                    IN_ARRAY -> attributeValue.value in conditionValue.values
                    NOT_IN_ARRAY -> (attributeValue.value in conditionValue.values).not()
                    else -> false
                }
            }

            // @TODO: handle dates

            else -> false
        }
    }

    fun allConditionsAreMatched(condition: Condition, context: Context): Boolean {
        return when (condition) {
            is Plain -> conditionIsMatched(condition.condition, context)

            is Multiple -> condition.conditions.all {
                allConditionsAreMatched(condition, context)
            }

            is And -> condition.condition.and.all {
                allConditionsAreMatched(condition, context)
            }

            is Or -> condition.condition.or.any {
                allConditionsAreMatched(condition, context)
            }

            is Not -> condition.condition.not.all {
                allConditionsAreMatched(condition, context)
            }.not()
        }
    }

    private fun compareVersions(actual: String, condition: String): Int =
        SemVer.parse(actual).compareTo(SemVer.parse(condition))
}
