package com.featurevisor.testRunner

import com.featurevisor.types.*

fun generateCombinations(
    keys: List<String>,
    matrix: AssertionMatrix,
    idx: Int,
    prev: MutableMap<String, Any>,
    combinations: MutableList<MutableMap<String, Any>>
) {
    val key = keys[idx]
    val values = matrix[key] ?: emptyList()

    for (i in values.indices) {
        val combination = prev.toMutableMap().apply { put(key, values[i]) }

        if (idx == keys.size - 1) {
            combinations.add(combination)
        } else {
            generateCombinations(keys, matrix, idx + 1, combination, combinations)
        }
    }
}

fun getMatrixCombinations(matrix: AssertionMatrix): List<MutableMap<String, Any>> {
    val keys = matrix.keys.toList()

    if (keys.isEmpty()) {
        return emptyList()
    }

    val combinations = mutableListOf<MutableMap<String, Any>>()
    generateCombinations(keys, matrix, 0, mutableMapOf(), combinations)
    return combinations
}

fun applyCombinationToValue(value: Any?, combination: Map<String, Any>): Any? {
    if (value is String) {
        val variableKeysInValue = Regex("""\$\{\{\s*([^\s}]+)\s*}}""").findAll(value)

        if (variableKeysInValue.none()) {
            return value
        }

        return variableKeysInValue.fold(value) { acc, result ->
            val key = result.groupValues[1].trim()
            val regex = Regex("""\$\{\{\s*([^\s}]+)\s*}}""")
            acc.replace(regex, combination[key].toString())
        }
    }
    return value
}

fun applyCombinationToFeatureAssertion(
    combination: Map<String, Any>,
    assertion: FeatureAssertion
): FeatureAssertion {
    val flattenedAssertion = assertion.copy()

    flattenedAssertion.environment = applyCombinationToValue(
        flattenedAssertion.environment,
        combination
    ) as EnvironmentKey

    flattenedAssertion.context =
        flattenedAssertion.context.mapValues { (_, value) ->
            getContextValue(applyCombinationToValue(getContextValues(value), combination))
        } as Context

    flattenedAssertion.at = applyCombinationToValue(getAtValue(flattenedAssertion.at).toString(), combination)?.let {
        if (it is String) {
            if (it.contains(".")) {
                WeightType.DoubleType(it.toDouble())
            } else {
                WeightType.IntType(it.toInt())
            }
        } else it
    } as WeightType

    flattenedAssertion.description = applyCombinationToValue(
        flattenedAssertion.description,
        combination
    ) as? String

    return flattenedAssertion
}

fun getFeatureAssertionsFromMatrix(
    aIndex: Int,
    assertionWithMatrix: FeatureAssertion
): List<FeatureAssertion> {
    if (assertionWithMatrix.matrix == null) {
        val assertion = assertionWithMatrix.copy()
        assertion.description = "Assertion #${aIndex + 1}: (${assertion.environment}) ${
            assertion.description ?: "at ${getAtValue(assertion.at)}%"
        }"
        return listOf(assertion)
    }

    val assertions = mutableListOf<FeatureAssertion>()
    val combinations = getMatrixCombinations(assertionWithMatrix.matrix)

    for (combination in combinations) {
        val assertion = applyCombinationToFeatureAssertion(combination, assertionWithMatrix)
        assertion.description = "Assertion #${aIndex + 1}: (${assertion.environment}) ${
            assertion.description ?: "at ${getAtValue(assertion.at)}%"
        }"
        assertions.add(assertion)
    }

    return assertions
}

@Suppress("IMPLICIT_CAST_TO_ANY")
fun getAtValue(at: WeightType) = when (at) {
    is WeightType.IntType -> {
        at.value
    }

    is WeightType.DoubleType -> {
        at.value
    }

    is WeightType.StringType -> {
        at.value
    }
}

fun applyCombinationToSegmentAssertion(
    combination: Map<String, Any>,
    assertion: SegmentAssertion
): SegmentAssertion {
    val flattenedAssertion = assertion.copy()

    flattenedAssertion.context = flattenedAssertion.context.mapValues { (_, value) ->
        getContextValue(applyCombinationToValue(getContextValues(value), combination))
    } as Context

    flattenedAssertion.description = applyCombinationToValue(
        flattenedAssertion.description,
        combination
    ) as? String

    return flattenedAssertion
}

fun getSegmentAssertionsFromMatrix(
    aIndex: Int,
    assertionWithMatrix: SegmentAssertion
): List<SegmentAssertion> {
    if (assertionWithMatrix.matrix == null) {
        val assertion = assertionWithMatrix.copy()
        assertion.description = "Assertion #${aIndex + 1}: ${assertion.description ?: "#${aIndex + 1}"}"
        return listOf(assertion)
    }

    val assertions = mutableListOf<SegmentAssertion>()
    val combinations = getMatrixCombinations(assertionWithMatrix.matrix)

    for (combination in combinations) {
        val assertion = applyCombinationToSegmentAssertion(combination, assertionWithMatrix)
        assertion.description = "Assertion #${aIndex + 1}: ${assertion.description ?: "#${aIndex + 1}"}"
        assertions.add(assertion)
    }

    return assertions
}
