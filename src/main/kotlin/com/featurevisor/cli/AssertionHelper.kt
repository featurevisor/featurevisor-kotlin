package com.featurevisor.cli

import com.featurevisor.sdk.*
import com.featurevisor.types.FeatureAssertion
import com.featurevisor.types.VariableValue

 internal fun assertExpectedToBeEnabled(actualValue: Boolean?, expectedValue: Boolean?) =
    (actualValue ?: false) == (expectedValue ?: false)

internal fun assertVariables(featureName: String, assertion: FeatureAssertion, featurevisorInstance: FeaturevisorInstance?) =
    assertion.expectedVariables?.map { (key, value) ->
        when (value) {
            is VariableValue.BooleanValue -> {
                val actualValue = featurevisorInstance?.getVariableBoolean(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
                value.value == actualValue
            }

            is VariableValue.IntValue -> {
              value.value == featurevisorInstance?.getVariableInteger(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
            }

            is VariableValue.DoubleValue -> {
                value.value == featurevisorInstance?.getVariableDouble(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
            }

            is VariableValue.StringValue -> {
               value.value ==  featurevisorInstance?.getVariableString(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
            }

            is VariableValue.ArrayValue -> {
                val variableValue = featurevisorInstance?.getVariableArray(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )

                if ((variableValue as List<*>).isEmpty()) {
                    true
                } else {
                    variableValue == value.values
                }
            }

            is VariableValue.JsonValue -> {
                val variableValue = featurevisorInstance?.getVariable(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
                (variableValue as VariableValue.JsonValue).value.equals(value.toString(), true)
            }

            is VariableValue.ObjectValue -> {
                val variableValue = featurevisorInstance?.getVariable(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
                variableValue == value.value
            }
        }
    }

 internal fun assertVariation(actualVariation: String?, expectedVariation: String?) =
    actualVariation?.equals(expectedVariation, true) ?: false
