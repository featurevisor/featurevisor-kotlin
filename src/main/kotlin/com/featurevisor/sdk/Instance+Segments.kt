package com.featurevisor.sdk

import com.featurevisor.types.Context
import com.featurevisor.types.FeatureKey
import com.featurevisor.types.GroupSegment
import com.featurevisor.types.PlainGroupSegment
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

fun FeaturevisorInstance.allGroupSegmentsAreMatched(
    groupSegments: GroupSegment,
    context: Context,
    datafileReader: DatafileReader
): Boolean {
    when (groupSegments) {
        is GroupSegment.Plain -> {
            val segmentKey = groupSegments.segment
            if (segmentKey == "*") {
                return true
            }

            val segment = datafileReader.getSegment(segmentKey)
            return segmentIsMatched(segment, context)
        }
        is GroupSegment.Multiple -> {
            return groupSegments.segments.all {
                allGroupSegmentsAreMatched(it, context, datafileReader)
            }
        }
        is GroupSegment.And -> {
            return groupSegments.segment.and.all {
                allGroupSegmentsAreMatched(it, context, datafileReader)
            }
        }
        is GroupSegment.Or -> {
            return groupSegments.segment.or.any {
                allGroupSegmentsAreMatched(it, context, datafileReader)
            }
        }
        is GroupSegment.Not -> {
            return groupSegments.segment.not.all {
                !allGroupSegmentsAreMatched(it, context, datafileReader)
            }
        }
    }
}
