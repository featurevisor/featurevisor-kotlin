package com.featurevisor.sdk

import com.featurevisor.sdk.Conditions.allConditionsAreMatched
import com.featurevisor.types.Context
import com.featurevisor.types.Feature
import com.featurevisor.types.Force

fun FeaturevisorInstance.getFeatureByKey(featureKey: String): Feature? {
    return datafileReader?.getFeature(featureKey)
}

fun FeaturevisorInstance.findForceFromFeature(
    feature: Feature,
    context: Context,
    datafileReader: DatafileReader
): Force? {

    return feature.force?.firstOrNull { force ->
        when {
            force.conditions != null -> allConditionsAreMatched(force.conditions, context)
            force.segments != null -> allGroupSegmentsAreMatched(force.segments, context, datafileReader)
            else -> false
        }
    }
}

fun FeaturevisorInstance.getMatchedTraffic(
    traffic: List<Traffic>,
    context: Context,
    datafileReader: DatafileReader
): Traffic? {

    return traffic.firstOrNull { trafficItem ->
        if (!allGroupSegmentsAreMatched(trafficItem.segments, context, datafileReader)) {
            return false
        }

        true
    }
}

fun FeaturevisorInstance.getMatchedAllocation(
    traffic: Traffic,
    bucketValue: Int
): Allocation? {

    return traffic.allocation.firstOrNull { allocation ->
        val start = allocation.range.start
        val end = allocation.range.end

        start <= bucketValue && end >= bucketValue
    }
}

data class MatchedTrafficAndAllocation(
    val matchedTraffic: Traffic?,
    val matchedAllocation: Allocation?
)

fun FeaturevisorInstance.getMatchedTrafficAndAllocation(
    traffic: List<Traffic>,
    context: Context,
    bucketValue: Int,
    datafileReader: DatafileReader,
    logger: Logger
): MatchedTrafficAndAllocation {

    var matchedAllocation: Allocation? = null

    val matchedTraffic = traffic.firstOrNull { trafficItem ->
        if (!allGroupSegmentsAreMatched(trafficItem.segments, context, datafileReader)) {
            return false
        }

        matchedAllocation = getMatchedAllocation(trafficItem, bucketValue)

        matchedAllocation != null
    }

    return MatchedTrafficAndAllocation(matchedTraffic, matchedAllocation)
}
