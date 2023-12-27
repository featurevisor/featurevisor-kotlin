package com.featurevisor.sdk

import com.featurevisor.types.Context
import com.featurevisor.types.FeatureKey
import com.featurevisor.types.VariableKey
import com.featurevisor.types.VariableValue
import com.featurevisor.types.VariableValue.ArrayValue
import com.featurevisor.types.VariableValue.BooleanValue
import com.featurevisor.types.VariableValue.DoubleValue
import com.featurevisor.types.VariableValue.IntValue
import com.featurevisor.types.VariableValue.JsonValue
import com.featurevisor.types.VariableValue.ObjectValue
import com.featurevisor.types.VariableValue.StringValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

fun FeaturevisorInstance.getVariable(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context = emptyMap(),
): VariableValue? {
    val evaluation = evaluateVariable(
        featureKey = featureKey,
        variableKey = variableKey,
        context = context
    )

    return evaluation.variableValue
}

fun FeaturevisorInstance.getVariableBoolean(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): Boolean? {
    return (getVariable(featureKey, variableKey, context) as? BooleanValue)?.value
}

fun FeaturevisorInstance.getVariableString(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): String? {
    return (getVariable(featureKey, variableKey, context) as? StringValue)?.value
}

fun FeaturevisorInstance.getVariableInteger(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): Int? {
    return (getVariable(featureKey, variableKey, context) as? IntValue)?.value
}

fun FeaturevisorInstance.getVariableDouble(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): Double? {
    return (getVariable(featureKey, variableKey, context) as? DoubleValue)?.value
}

fun FeaturevisorInstance.getVariableArray(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): List<String>? {
    return (getVariable(featureKey, variableKey, context) as? ArrayValue)?.values
}

inline fun <reified T : Any> FeaturevisorInstance.getVariableObject(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): T? {
    val objectValue = getVariable(featureKey, variableKey, context) as? ObjectValue
    return try {
        val encoded = Json.encodeToJsonElement(objectValue?.value)
        return Json.decodeFromJsonElement<T>(encoded)
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T : Any> FeaturevisorInstance.getVariableJSON(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): T? {
    val json = getVariable(featureKey, variableKey, context) as? JsonValue
    return try {
        Json.decodeFromString<T>(json!!.value)
    } catch (e: Exception) {
        null
    }
}

