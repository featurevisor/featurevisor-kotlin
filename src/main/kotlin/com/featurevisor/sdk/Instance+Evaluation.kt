package com.featurevisor.sdk

import com.featurevisor.sdk.Conditions.allConditionsAreMatched
import com.featurevisor.sdk.EvaluationReason.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

enum class EvaluationReason(val value: String) {
    NOT_FOUND("not_found"),
    NO_VARIATIONS("no_variations"),
    DISABLED("disabled"),
    REQUIRED("required"),
    OUT_OF_RANGE("out_of_range"),
    FORCED("forced"),
    INITIAL("initial"),
    STICKY("sticky"),
    RULE("rule"),
    ALLOCATED("allocated"),
    DEFAULTED("defaulted"),
    OVERRIDE("override"),
    ERROR("error")
}

@Serializable
data class Evaluation(
    val featureKey: FeatureKey,
    val reason: EvaluationReason,
    val bucketValue: BucketValue? = null,
    val ruleKey: RuleKey? = null,
    val enabled: Boolean? = null,
    val traffic: Traffic? = null,
    val sticky: OverrideFeature? = null,
    val initial: OverrideFeature? = null,
    val variation: Variation? = null,
    val variationValue: VariationValue? = null,
    val variableKey: VariableKey? = null,
    val variableValue: VariableValue? = null,
    val variableSchema: VariableSchema? = null,
) {
    fun toDictionary(): Map<String, Any> {
        val data = try {
            val json = Json.encodeToJsonElement(this)
            Json.decodeFromJsonElement<Map<String, Any>>(json)
        } catch (e: Exception) {
            emptyMap()
        }

        return data
    }
}

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
            reason = DISABLED,
        )

        logger?.debug("feature is disabled", evaluation.toDictionary())
        return evaluation
    }

    // sticky
    stickyFeatures?.get(featureKey)?.variation?.let { variationValue ->
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = STICKY,
            variationValue = variationValue,
        )

        logger?.debug("using sticky variation", evaluation.toDictionary())
        return evaluation
    }

    // initial
    if (statuses.ready.not() && initialFeatures?.get(featureKey)?.variation != null) {
        val variationValue = initialFeatures[featureKey]?.variation
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = INITIAL,
            variationValue = variationValue
        )

        logger?.debug("using initial variation", evaluation.toDictionary())
        return evaluation
    }

    val feature = getFeatureByKey(featureKey)
    if (feature == null) {
        // not found
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = NOT_FOUND
        )

        logger?.warn("feature not found", evaluation.toDictionary())
        return evaluation
    }

    if (feature.variations.isNullOrEmpty()) {
        // no variations
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = NO_VARIATIONS
        )

        logger?.warn("no variations", evaluation.toDictionary())
        return evaluation
    }

    val finalContext = interceptContext?.invoke(context) ?: context

    // forced
    val force = findForceFromFeature(feature, context, datafileReader)
    if (force != null) {
        val variation = feature.variations.firstOrNull { it.value == force.variation }

        if (variation != null) {
            evaluation = Evaluation(
                featureKey = feature.key,
                reason = FORCED,
                variation = variation
            )

            logger?.debug("forced variation found", evaluation.toDictionary())

            return evaluation
        }
    }

    // bucketing
    val bucketValue = getBucketValue(feature, finalContext)

    val matchedTrafficAndAllocation = getMatchedTrafficAndAllocation(
        feature.traffic,
        finalContext,
        bucketValue,
        datafileReader,
        logger
    )

    val matchedTraffic = matchedTrafficAndAllocation.matchedTraffic

    // override from rule
    if (matchedTraffic?.variation != null) {
        val variation = feature.variations?.firstOrNull { it.value == matchedTraffic.variation }
        if (variation != null) {
            evaluation = Evaluation(
                featureKey = feature.key,
                reason = RULE,
                bucketValue = bucketValue,
                ruleKey = matchedTraffic.key,
                variation = variation
            )

            logger?.debug("override from rule", evaluation.toDictionary())

            return evaluation
        }
    }

    val matchedAllocation = matchedTrafficAndAllocation.matchedAllocation

    // regular allocation
    if (matchedAllocation != null) {
        val variation = feature.variations?.firstOrNull { it.value == matchedAllocation.variation }
        if (variation != null) {
            evaluation = Evaluation(
                featureKey = feature.key,
                reason = ALLOCATED,
                bucketValue = bucketValue,
                variation = variation
            )

            logger?.debug("allocated variation", evaluation.toDictionary())

            return evaluation
        }
    }

    // nothing matched
    evaluation = Evaluation(
        featureKey = feature.key,
        reason = ERROR,
        bucketValue = bucketValue
    )

    logger?.debug("no matched variation", evaluation.toDictionary())

    return evaluation
}

fun FeaturevisorInstance.evaluateFlag(featureKey: FeatureKey, context: Context = emptyMap()): Evaluation {
    val evaluation: Evaluation

    // sticky
    stickyFeatures?.get(featureKey)?.let { stickyFeature ->
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = STICKY,
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
            reason = INITIAL,
            enabled = initialFeature?.enabled,
            initial = initialFeature
        )

        logger?.debug("using initial enabled", evaluation.toDictionary())

        return evaluation
    }

    val feature = getFeatureByKey(featureKey)
    if (feature == null) {
        // not found
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = NOT_FOUND
        )

        logger?.warn("feature not found", evaluation.toDictionary())
        return evaluation
    }

    // deprecated
    if (feature.deprecated == true) {
        logger?.warn("feature is deprecated", mapOf("featureKey" to feature.key))
    }

    val finalContext = interceptContext?.invoke(context) ?: context

    // forced
    findForceFromFeature(feature, context, datafileReader)?.let { force ->
        if (force.enabled != null) {
            evaluation = Evaluation(
                featureKey = featureKey,
                reason = FORCED,
                enabled = force.enabled
            )

            logger?.debug("forced enabled found", evaluation.toDictionary())

            return evaluation
        }
    }

    // required
    if (feature.required.isNullOrEmpty().not()) {
        val requiredFeaturesAreEnabled = feature.required!!.all { item ->
            var requiredKey: FeatureKey
            var requiredVariation: VariationValue?
            when (item) {
                is Required.FeatureKey -> {
                    requiredKey = item.required
                    requiredVariation = null
                }

                is Required.WithVariation -> {
                    requiredKey = item.required.key
                    requiredVariation = item.required.variation
                }
            }

            val requiredIsEnabled = isEnabled(requiredKey, finalContext)

            if (requiredIsEnabled.not()) {
                return@all false
            }

            val requiredVariationValue = getVariation(requiredKey, finalContext)

            return@all requiredVariationValue == requiredVariation
        }

        if (requiredFeaturesAreEnabled.not()) {
            evaluation = Evaluation(
                featureKey = feature.key,
                reason = REQUIRED,
                enabled = requiredFeaturesAreEnabled
            )

            return evaluation
        }
    }

    // bucketing
    val bucketValue = getBucketValue(feature = feature, context = finalContext)

    val matchedTraffic = getMatchedTraffic(
        traffic = feature.traffic,
        context = finalContext,
        datafileReader = datafileReader,
    )

    if (matchedTraffic != null) {

        if (feature.ranges.isNullOrEmpty().not()) {

            val matchedRange = feature.ranges!!.firstOrNull { range ->
                bucketValue >= range.start && bucketValue < range.end
            }

            // matched
            if (matchedRange != null) {
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = ALLOCATED,
                    bucketValue = bucketValue,
                    enabled = matchedTraffic.enabled ?: true
                )

                return evaluation
            }

            // no match
            evaluation = Evaluation(
                featureKey = feature.key,
                reason = OUT_OF_RANGE,
                bucketValue = bucketValue,
                enabled = false
            )

            logger?.debug("not matched", evaluation.toDictionary())

            return evaluation
        }

        // override from rule
        val matchedTrafficEnabled = matchedTraffic.enabled
        if (matchedTrafficEnabled != null) {
            evaluation = Evaluation(
                featureKey = feature.key,
                reason = OVERRIDE,
                bucketValue = bucketValue,
                ruleKey = matchedTraffic.key,
                enabled = matchedTrafficEnabled,
                traffic = matchedTraffic
            )

            logger?.debug("override from rule", evaluation.toDictionary())

            return evaluation
        }

        // treated as enabled because of matched traffic
        if (bucketValue < matchedTraffic.percentage) {
            // @TODO: verify if range check should be inclusive or not
            evaluation = Evaluation(
                featureKey = feature.key,
                reason = RULE,
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
        reason = ERROR,
        bucketValue = bucketValue,
        enabled = false
    )

    return evaluation

}

fun FeaturevisorInstance.evaluateVariable(
    featureKey: FeatureKey,
    variableKey: VariableKey,
    context: Context = emptyMap(),
): Evaluation {

    val evaluation: Evaluation
    val flag = evaluateFlag(featureKey, context)
    if (flag.enabled == false) {
        evaluation = Evaluation(featureKey = featureKey, reason = DISABLED)
        logger?.debug("feature is disabled", evaluation.toDictionary())
        return evaluation
    }

    // sticky
    stickyFeatures?.get(featureKey)?.variables?.get(variableKey)?.let { variableValue ->
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = STICKY,
            variableKey = variableKey,
            variableValue = variableValue
        )

        logger?.debug("using sticky variable", evaluation.toDictionary())
        return evaluation
    }

    // initial
    if (!statuses.ready && initialFeatures?.get(featureKey)?.variables?.get(variableKey) != null) {
        val variableValue = initialFeatures?.get(featureKey)?.variables?.get(variableKey)
        evaluation = Evaluation(
            featureKey = featureKey,
            reason = INITIAL,
            variableKey = variableKey,
            variableValue = variableValue
        )

        logger?.debug("using initial variable", evaluation.toDictionary())
        return evaluation
    }

    getFeatureByKey(featureKey).let { feature ->
        if (feature == null) {
            evaluation = Evaluation(
                featureKey = featureKey,
                reason = NOT_FOUND,
                variableKey = variableKey
            )

            logger?.warn("feature not found in datafile", evaluation.toDictionary())
            return evaluation
        }

        val variableSchema = feature.variablesSchema?.firstOrNull { variableSchema ->
            variableSchema.key == variableKey
        }

        if (variableSchema == null) {
            evaluation = Evaluation(
                featureKey = featureKey,
                reason = NOT_FOUND,
                variableKey = variableKey
            )

            logger?.warn("variable schema not found", evaluation.toDictionary())
            return evaluation
        }

        val finalContext = interceptContext?.invoke(context) ?: context

        // forced
        findForceFromFeature(feature, context, datafileReader)?.let { force ->
            if (force.variables?.containsKey(variableKey) == true) {
                val variableValue = force.variables[variableKey]
                evaluation = Evaluation(
                    featureKey = feature.key,
                    reason = FORCED,
                    variableKey = variableKey,
                    variableValue = variableValue,
                    variableSchema = variableSchema
                )

                logger?.debug("forced variable", evaluation.toDictionary())
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
                    reason = RULE,
                    bucketValue = bucketValue,
                    ruleKey = matchedTraffic.key,
                    variableKey = variableKey,
                    variableValue = variableValue,
                    variableSchema = variableSchema
                )

                logger?.debug("override from rule", evaluation.toDictionary())

                return evaluation
            }

            // regular allocation
            matchedTrafficAndAllocation.matchedAllocation?.let { matchedAllocation ->
                val variation = feature.variations?.firstOrNull { variation ->
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
                        reason = OVERRIDE,
                        bucketValue = bucketValue,
                        ruleKey = matchedTraffic.key,
                        variableKey = variableKey,
                        variableValue = override.value,
                        variableSchema = variableSchema
                    )

                    logger?.debug("variable override", evaluation.toDictionary())
                    return evaluation
                }

                if (variableFromVariation?.value != null) {
                    evaluation = Evaluation(
                        featureKey = feature.key,
                        reason = ALLOCATED,
                        bucketValue = bucketValue,
                        ruleKey = matchedTraffic.key,
                        variableKey = variableKey,
                        variableValue = variableFromVariation.value,
                        variableSchema = variableSchema
                    )

                    logger?.debug("allocated variable", evaluation.toDictionary())
                    return evaluation
                }
            }
        }

        // fall back to default
        evaluation = Evaluation(
            featureKey = feature.key,
            reason = DEFAULTED,
            bucketValue = bucketValue,
            variableKey = variableKey,
            variableValue = variableSchema.defaultValue,
            variableSchema = variableSchema
        )

        logger?.debug("using default value", evaluation.toDictionary())
        return evaluation
    }
}

private fun FeaturevisorInstance.getBucketKey(feature: Feature, context: Context): BucketKey {
    val featureKey = feature.key
    var type: String
    var attributeKeys: List<AttributeKey>

    when (val bucketBy = feature.bucketBy) {
        is BucketBy.Single -> {
            type = "plain"
            attributeKeys = listOf(bucketBy.bucketBy)
        }

        is BucketBy.And -> {
            type = "and"
            attributeKeys = bucketBy.bucketBy
        }

        is BucketBy.Or -> {
            type = "or"
            attributeKeys = bucketBy.bucketBy
        }
    }

    val bucketKey: MutableList<AttributeValue> = mutableListOf()
    attributeKeys.forEach { attributeKey ->
        val attributeValue = context[attributeKey]
        if (attributeValue != null) {
            if (type == "plain" || type == "and") {
                bucketKey.add(attributeValue)
            } else {  // or
                if (bucketKey.isEmpty()) {
                    bucketKey.add(attributeValue)
                }
            }
        }
    }

    bucketKey.add(AttributeValue.StringValue(featureKey))

    val result = bucketKey.map {
        it.toString()
    }.joinToString(separator = bucketKeySeparator)

    configureBucketKey?.let { configureBucketKey ->
        return configureBucketKey(feature, context, result)
    }

    return result
}

private fun FeaturevisorInstance.getBucketValue(feature: Feature, context: Context): BucketValue {
    val bucketKey = getBucketKey(feature, context)
    val value = Bucket.getBucketedNumber(bucketKey)

    configureBucketValue?.let { configureBucketValue ->
        return configureBucketValue(feature, context, value)
    }

    return value
}
