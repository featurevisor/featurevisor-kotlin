package com.featurevisor.sdk

import com.featurevisor.sdk.Conditions.allConditionsAreMatched
import com.featurevisor.types.Context
import com.featurevisor.types.FeatureKey
import com.featurevisor.types.GroupSegment
import com.featurevisor.types.GroupSegment.*
import com.featurevisor.types.Segment
import com.featurevisor.types.VariationValue

fun FeaturevisorInstance.segmentIsMatched(featureKey: FeatureKey, context: Context): VariationValue? {
    val evaluation = evaluateVariation(featureKey, context)

    if (evaluation.variationValue != null) {
        return evaluation.variationValue
    }

    if (evaluation.variation != null) {
        return evaluation.variation.value
    }

    return null
}

fun FeaturevisorInstance.segmentIsMatched(segment: Segment, context: Context): Boolean {
    return allConditionsAreMatched(segment.conditions, context)
}

fun FeaturevisorInstance.allGroupSegmentsAreMatched(
    groupSegments: GroupSegment,
    context: Context,
    datafileReader: DatafileReader
): Boolean {
    return when (groupSegments) {
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
}
