package com.featurevisor.cli

import com.featurevisor.sdk.*
import com.featurevisor.types.Assertion
import com.featurevisor.types.VariableValue

 internal fun assertExpectedToBeEnabled(actualValue: Boolean?, expectedValue: Boolean?) =
    (actualValue ?: false) == (expectedValue ?: false)

internal fun assertVariables(featureName: String, assertion: Assertion, featurevisorInstance: FeaturevisorInstance?) =
    assertion.expectedVariables?.map { (key, value) ->
        when (value) {
            is VariableValue.BooleanValue -> {
                value == featurevisorInstance?.getVariableBoolean(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
            }

            is VariableValue.IntValue -> {
                value == featurevisorInstance?.getVariableInteger(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
            }

            is VariableValue.DoubleValue -> {
                value == featurevisorInstance?.getVariableDouble(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
            }

            is VariableValue.StringValue -> {
                value == featurevisorInstance?.getVariableString(
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
                    variableValue == value
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
                variableValue == value
            }
            else -> {
                val variableValue = featurevisorInstance?.getVariable(
                    featureKey = featureName,
                    variableKey = key,
                    context = assertion.context
                )
               variableValue == value
            }
        }
    }

 internal fun assertVariation(actualVariation: String?, expectedVariation: String?) =
    actualVariation?.equals(expectedVariation, true) ?: false

internal fun assertSegment(projectRootPath:String, assertion: Assertion,segmentName:String):Boolean{
    val yamlSegment = parseYamlSegment("$projectRootPath/segments/$segmentName.yml")
    val isSegmentMatched = segmentIsMatched(yamlSegment!!, assertion.context)
    return isSegmentMatched == assertion.expectedToMatch
}
