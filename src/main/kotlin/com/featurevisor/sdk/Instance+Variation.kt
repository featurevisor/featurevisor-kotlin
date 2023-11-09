package com.featurevisor.sdk

import com.featurevisor.sdk.types.Context
import com.featurevisor.sdk.types.FeatureKey
import com.featurevisor.sdk.types.VariationValue

internal fun FeaturevisorInstance.getVariation(featureKey: FeatureKey, context: Context): VariationValue? {
    val evaluation = evaluateVariation(featureKey, context)
    return when {
        evaluation.variationValue != null -> evaluation.variationValue
        evaluation.variation != null -> evaluation.variation.value
        else -> null
    }
}
