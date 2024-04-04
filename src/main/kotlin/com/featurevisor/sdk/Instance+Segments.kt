package com.featurevisor.sdk

import com.featurevisor.sdk.Conditions.allConditionsAreMatched
import com.featurevisor.types.Context
import com.featurevisor.types.FeatureKey
import com.featurevisor.types.GroupSegment
import com.featurevisor.types.GroupSegment.*
import com.featurevisor.types.Segment
import com.featurevisor.types.VariationValue

internal fun FeaturevisorInstance.segmentIsMatched(
    featureKey: FeatureKey,
    context: Context,
): VariationValue? {
    return try {
        val evaluation = evaluateVariation(featureKey, context)
        var variationValue: VariationValue? = null

        if (evaluation.variationValue != null) {
            variationValue = evaluation.variationValue
        }

        if (evaluation.variation != null) {
            variationValue = evaluation.variation.value
        }

        variationValue
    } catch (e: Exception) {
        null
    }
}

internal fun segmentIsMatched(segment: Segment, context: Context): Boolean {
    return allConditionsAreMatched(segment.conditions, context)
}

internal fun FeaturevisorInstance.allGroupSegmentsAreMatched(
    groupSegments: GroupSegment,
    context: Context,
    datafileReader: DatafileReader,
): Boolean {
    return try {
        when (groupSegments) {
            is Plain -> {
                val segmentKey = groupSegments.segment
                if (segmentKey == "*") {
                    true
                } else {
                    datafileReader.getSegment(segmentKey)?.let {
                        segmentIsMatched(it, context)
                    } ?: false
                }
            }

            is Multiple -> {
                groupSegments.segments.all {
                    allGroupSegmentsAreMatched(it, context, datafileReader)
                }
            }

            is And -> {
                groupSegments.segment.and.all {
                    allGroupSegmentsAreMatched(it, context, datafileReader)
                }
            }

            is Or -> {
                groupSegments.segment.or.any {
                    allGroupSegmentsAreMatched(it, context, datafileReader)
                }
            }

            is Not -> {
                groupSegments.segment.not.all {
                    allGroupSegmentsAreMatched(it, context, datafileReader).not()
                }
            }
        }
    }catch (e:Exception){
        false
    }
}
