package com.featurevisor.sdk

import com.featurevisor.sdk.VariableValue.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

internal fun FeaturevisorInstance.getVariable(
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

internal fun FeaturevisorInstance.getVariableBoolean(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): Boolean? {
    return (getVariable(featureKey, variableKey, context) as? BooleanValue)?.value
}

internal fun FeaturevisorInstance.getVariableString(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): String? {
    return (getVariable(featureKey, variableKey, context) as? StringValue)?.value
}

internal fun FeaturevisorInstance.getVariableInteger(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): Int? {
    return (getVariable(featureKey, variableKey, context) as? IntValue)?.value
}

internal fun FeaturevisorInstance.getVariableDouble(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): Double? {
    return (getVariable(featureKey, variableKey, context) as? DoubleValue)?.value
}

internal fun FeaturevisorInstance.getVariableArray(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context,
): List<String>? {
    return (getVariable(featureKey, variableKey, context) as? ArrayValue)?.values
}

internal inline fun <reified T : Any> FeaturevisorInstance.getVariableObject(
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

internal inline fun <reified T : Any> FeaturevisorInstance.getVariableJSON(
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
