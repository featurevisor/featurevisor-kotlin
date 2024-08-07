package com.featurevisor.sdk

import com.featurevisor.types.Context
import com.featurevisor.types.FeatureKey
import com.featurevisor.types.VariationValue

fun FeaturevisorInstance.getVariation(featureKey: FeatureKey, context: Context): VariationValue? {
    val evaluation = evaluateVariation(featureKey, context)
    return when {
        evaluation.variationValue != null -> evaluation.variationValue
        evaluation.variation != null -> evaluation.variation.value
        else -> null
    }
}
