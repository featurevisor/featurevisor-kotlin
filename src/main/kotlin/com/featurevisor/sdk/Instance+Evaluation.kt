package com.featurevisor.sdk

import com.featurevisor.types.Context
import com.featurevisor.types.FeatureKey

fun FeaturevisorInstance.isEnabled(featureKey: FeatureKey, context: Context = emptyMap()): Boolean {
    val evaluation = evaluateFlag(featureKey, context)
    return evaluation.enabled == true
}

fun FeaturevisorInstance.evaluateVariation(featureKey: FeatureKey, context: Context = emptyMap()): Evaluation {
    var evaluation: Evaluation
    val flag = evaluateFlag(featureKey, context)
    if (flag.enabled == false) {
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = EvaluationReason.DISABLED
        )

        logger?.debug("feature is disabled", evaluation.toDictionary())
        return evaluation
    }

    // sticky
    stickyFeatures?.get(featureKey)?.variation?.let { variationValue ->
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = EvaluationReason.STICKY,
            variationValue = variationValue
        )

        logger?.debug("using sticky variation", evaluation.toDictionary())
        return evaluation
    }

    // initial
    if (!statuses.ready && initialFeatures?.get(featureKey)?.variation != null) {
        val variationValue = initialFeatures[featureKey]?.variation
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = EvaluationReason.INITIAL,
            variationValue = variationValue
        )

        logger?.debug("using initial variation", evaluation.toDictionary())
        return evaluation
    }

    getFeature(featureKey)?.let { feature ->
        if (feature.variations.isEmpty) {
            evaluation = Evaluation(
                featureKey = featureKey,
                reason = EvaluationReason.NO_VARIATIONS
            )

            logger.warn("no variations", evaluation.toDictionary())
            return evaluation
        }

        val finalContext = interceptContext?.invoke(context) ?: context

        // forced
        findForceFromFeature(feature, context, datafileReader)?.let { force ->
            val variation = feature.variations.firstOrNull { variation ->
                variation.value == force.variation
            }

            if (variation != null) {
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = EvaluationReason.FORCED,
                    variation = variation
                )

                logger.debug("forced variation found", evaluation.toDictionary())

                return evaluation
            }
        }

        // bucketing
        val bucketValue = getBucketValue(feature, finalContext)
        val matchedTrafficAndAllocation = getMatchedTrafficAndAllocation(
            traffic = feature.traffic,
            context = finalContext,
            bucketValue = bucketValue,
            datafileReader = datafileReader,
            logger = logger
        )

        if (matchedTrafficAndAllocation.matchedTraffic != null) {
            // override from rule
            val matchedTrafficVariationValue = matchedTrafficAndAllocation.matchedTraffic.variation

            val variation = feature.variations.firstOrNull { variation ->
                variation.value == matchedTrafficVariationValue
            }

            if (variation != null) {
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = EvaluationReason.RULE,
                    bucketValue = bucketValue,
                    ruleKey = matchedTrafficAndAllocation.matchedTraffic.key,
                    variation = variation
                )

                logger.debug("override from rule", evaluation.toDictionary())

                return evaluation
            }

            // regular allocation
            val matchedAllocation = matchedTrafficAndAllocation.matchedAllocation

            val variation = feature.variations.firstOrNull { variation ->
                variation.value == matchedAllocation.variation
            }

            if (variation != null) {
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = EvaluationReason.ALLOCATED,
                    bucketValue = bucketValue,
                    variation = variation
                )

                logger.debug("allocated variation", evaluation.toDictionary())

                return evaluation
            }
        }

        // nothing matched
        evaluation = Evaluation(
            featureKey = feature.key,
            reason = EvaluationReason.ERROR,
            bucketValue = bucketValue
        )

        logger.debug("no matched variation", evaluation.toDictionary())

        return evaluation
    }
}

fun FeaturevisorInstance.evaluateFlag(featureKey: FeatureKey, context: Context = emptyMap()): Evaluation {
    val evaluation: Evaluation

    // sticky
    stickyFeatures?.get(featureKey)?.let { stickyFeature ->
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = EvaluationReason.STICKY,
            enabled = stickyFeature.enabled,
            sticky = stickyFeature
        )

        logger?.debug("using sticky enabled", evaluation.toDictionary())

        return evaluation
    }

    // initial
    if (statuses.ready && initialFeatures?.get(featureKey) != null) {
        val initialFeature = initialFeatures[featureKey]
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = EvaluationReason.INITIAL,
            enabled = initialFeature.enabled,
            initial = initialFeature
        )

        logger.debug("using initial enabled", evaluation.toDictionary())

        return evaluation
    }

    val feature = getFeature(featureKey)

    if (feature == null) {
        // not found
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = EvaluationReason.NOT_FOUND
        )

        logger.warn("feature not found", evaluation.toDictionary())

        return evaluation
    }

    // deprecated
    if (feature.deprecated == true) {
        logger.warn("feature is deprecated", mapOf("featureKey" to feature.key))
    }

    val finalContext = interceptContext?.invoke(context) ?: context

    // forced
    findForceFromFeature(feature, context, datafileReader)?.let { force ->
        if (force.enabled != null) {
            evaluation = Evaluation(
                featureKey = featureKey,
                reason = EvaluationReason.FORCED,
                enabled = force.enabled
            )

            logger.debug("forced enabled found", evaluation.toDictionary())

            return evaluation
        }
    }

    // required
    if (!feature.required.isEmpty()) {
        val requiredFeaturesAreEnabled = feature.required.all { item ->
            when (item) {
                is FeatureKey -> {
                    val requiredKey = item
                    val requiredVariation: VariationValue? = null
                    val requiredIsEnabled = isEnabled(requiredKey, finalContext)

                    if (!requiredIsEnabled) {
                        return@all false
                    }

                    if (requiredVariation != null) {
                        val requiredVariationValue = getVariation(requiredKey, finalContext)
                        return requiredVariationValue == requiredVariation
                    }

                    true
                }
                is WithVariation -> {
                    val variation = item
                    val requiredKey = variation.key
                    val requiredVariation = variation.variation
                    val requiredIsEnabled = isEnabled(requiredKey, finalContext)

                    if (!requiredIsEnabled) {
                        return@all false
                    }

                    if (requiredVariation != null) {
                        val requiredVariationValue = getVariation(requiredKey, finalContext)
                        return requiredVariationValue == requiredVariation
                    }

                    true
                }
            }
        }

        if (!requiredFeaturesAreEnabled) {
            evaluation = Evaluation(
                featureKey = feature.key,
                reason = EvaluationReason.REQUIRED,
                enabled = requiredFeaturesAreEnabled
            )

            return evaluation
        }
    }

    // bucketing
    val bucketValue = getBucketValue(feature, finalContext)
    val matchedTraffic = getMatchedTraffic(
        traffic = feature.traffic,
        context = finalContext,
        datafileReader = datafile
        if (matchedTraffic != null) {

            if (!feature.ranges.isEmpty()) {

                val matchedRange = feature.ranges.firstOrNull { range ->
                    bucketValue >= range.start && bucketValue < range.end
                }

                // matched
                if (matchedRange != null) {
                    evaluation = Evaluation(
                        featureKey = feature.key,
                        reason = EvaluationReason.ALLOCATED,
                        bucketValue = bucketValue,
                        enabled = matchedTraffic.enabled ?: true
                    )

                    return evaluation
                }

                // no match
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = EvaluationReason.OUT_OF_RANGE,
                    bucketValue = bucketValue,
                    enabled = false
                )

                logger.debug("not matched", evaluation.toDictionary())

                return evaluation
            }

            // override from rule
            matchedTraffic.enabled?.let { matchedTrafficEnabled ->
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = EvaluationReason.OVERRIDE,
                    bucketValue = bucketValue,
                    ruleKey = matchedTraffic.key,
                    enabled = matchedTrafficEnabled,
                    traffic = matchedTraffic
                )

                logger.debug("override from rule", evaluation.toDictionary())

                return evaluation
            }

            // treated as enabled because of matched traffic
            if (bucketValue < matchedTraffic.percentage) {
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = EvaluationReason.RULE,
                    bucketValue = bucketValue,
                    ruleKey = matchedTraffic.key,
                    enabled = true,
                    traffic = matchedTraffic
                )

                return evaluation
            }
        }

            // nothing matched
            evaluation = Evaluation(
            featureKey = feature.key,
        reason = EvaluationReason.ERROR,
        bucketValue = bucketValue,
        enabled = false
    )

    return evaluation
}
}

fun FeaturevisorInstance.evaluateVariable(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context = emptyMap()
): Evaluation {

    val evaluation: Evaluation

    val flag = evaluateFlag(featureKey, context)

    if (flag.enabled == false) {
        evaluation = Evaluation(featureKey = featureKey, reason = EvaluationReason.DISABLED)

        logger.debug("feature is disabled", evaluation.toDictionary())

        return evaluation
    }

    // sticky
    stickyFeatures?.get(featureKey)?.variables?.get(variableKey)?.let { variableValue ->
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = EvaluationReason.STICKY,
            variableKey = variableKey,
            variableValue = variableValue
        )

        logger.debug("using sticky variable", evaluation.toDictionary())

        return evaluation
    }

    // initial
    if (!statuses.ready && initialFeatures?.get(featureKey)?.variables?.get(variableKey) != null) {

        val variableValue = initialFeatures?.get(featureKey)?.variables?.get(variableKey)
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = EvaluationReason.INITIAL,
            variableKey = variableKey,
            variableValue = variableValue
        )

        logger.debug("using initial variable", evaluation.toDictionary())

        return evaluation
    }

    getFeature(featureKey)?.let { feature ->
        if (feature.variablesSchema.isEmpty()) {
            evaluation = Evaluation(
                featureKey = featureKey,
                reason = EvaluationReason.NOT_FOUND,
                variableKey = variableKey
            )

            logger.warn("feature not found in datafile", evaluation.toDictionary())

            return evaluation
        }

        val variableSchema = feature.variablesSchema.firstOrNull { variableSchema ->
            variableSchema.key == variableKey
        }

        if (variableSchema == null) {
            evaluation = Evaluation(
                featureKey = featureKey,
                reason = EvaluationReason.NOT_FOUND,
                variableKey = variableKey
            )

            logger.warn("variable schema not found", evaluation.toDictionary())

            return evaluation
        }

        val finalContext = interceptContext?.invoke(context) ?: context

        // forced
        findForceFromFeature(feature, context, datafileReader)?.let { force ->
            if (force.variables.containsKey(variableKey)) {
                val variableValue = force.variables[variableKey]
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = EvaluationReason.FORCED,
                    variableKey = variableKey,
                    variableValue = variableValue,
                    variableSchema = variableSchema
                )

                logger.debug("forced variable", evaluation.toDictionary())

                return evaluation
            }
        }

        // bucketing
        val bucketValue = getBucketValue(feature, finalContext)
        val matchedTrafficAndAllocation = getMatchedTrafficAndAllocation(
            traffic = feature.traffic,
            context = finalContext,
            bucketValue = bucketValue,
            datafileReader = datafileReader,
            logger = logger
        )

        matchedTrafficAndAllocation.matchedTraffic?.let { matchedTraffic ->
            // override from rule
            matchedTraffic.variables?.get(variableKey)?.let { variableValue ->
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = EvaluationReason.RULE,
                    bucketValue = bucketValue,
                    ruleKey = matchedTraffic.key,
                    variableKey = variableKey,
                    variableValue = variableValue,
                    variableSchema = variableSchema
                )

                logger.debug("override from rule", evaluation.toDictionary())

                return evaluation
            }

            // regular allocation
            matchedTrafficAndAllocation.matchedAllocation?.let { matchedAllocation ->
                val variation = feature.variations.firstOrNull { variation ->
                    variation.value == matchedAllocation.variation
                }

                val variableFromVariation = variation?.variables?.firstOrNull { variable ->
                    variable.key == variableKey
                }

                variableFromVariation?.overrides?.firstOrNull { override ->
                    if (override.conditions != null) {
                        return@firstOrNull allConditionsAreMatched(override.conditions, finalContext)
                    }

                    if (override.segments != null) {
                        return@firstOrNull allGroupSegmentsAreMatched(
                            override.segments,
                            finalContext,
                            datafileReader
                        )
                    }

                    false
                }?.let { override ->
                    evaluation = Evaluation(
                        featureKey = feature.key,
                        reason = EvaluationReason.OVERRIDE,
                        bucketValue = bucketValue,
                        ruleKey = matchedTraffic.key,
                        variableKey = variableKey,
                        variableValue = override.value,
                        variableSchema = variableSchema
                    )

                    logger.debug("variable override", evaluation.toDictionary())

                    return evaluation
                }

                if (variableFromVariation?.value != null) {
                    evaluation = Evaluation(
                        featureKey = feature.key,
                        reason = EvaluationReason.ALLOCATED,
                        bucketValue = bucketValue,
                        ruleKey = matchedTraffic.key,
                        variableKey = variableKey,
                        variableValue = variableFromVariation.value,
                        variableSchema = variableSchema
                    )

                    logger.debug("allocated variable", evaluation.toDictionary())

                    return evaluation
                }
            }
        }

        // fall back to default
        evaluation = Evaluation(
            featureKey = feature.key,
            reason = EvaluationReason.DEFAULTED,
            bucketValue = bucketValue,
            variableKey = variableKey,
            variableValue = variableSchema.defaultValue,
            variableSchema = variableSchema
        )

        logger.debug("using default value", evaluation.toDictionary())

        return evaluation
    }
}

// ... The rest of your Kotlin code
