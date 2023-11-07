package com.featurevisor.sdk

internal fun FeaturevisorInstance.getVariation(featureKey: FeatureKey, context: Context): VariationValue? {
    val evaluation = evaluateVariation(featureKey, context)
    return when {
        evaluation.variationValue != null -> evaluation.variationValue
        evaluation.variation != null -> evaluation.variation.value
        else -> null
    }
}
