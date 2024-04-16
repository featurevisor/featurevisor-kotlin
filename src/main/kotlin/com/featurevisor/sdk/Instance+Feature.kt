package com.featurevisor.sdk

import com.featurevisor.sdk.Conditions.allConditionsAreMatched
import com.featurevisor.types.Allocation
import com.featurevisor.types.Context
import com.featurevisor.types.Feature
import com.featurevisor.types.Force
import com.featurevisor.types.Traffic

public fun FeaturevisorInstance.getFeatureByKey(featureKey: String): Feature? {
    return datafileReader.getFeature(featureKey)
}

public fun FeaturevisorInstance.getFeature(featureKey: String): Feature?{
    return datafileReader.getFeature(featureKey)
}

internal fun FeaturevisorInstance.findForceFromFeature(
    feature: Feature,
    context: Context,
    datafileReader: DatafileReader,
): Force? {

    return feature.force?.firstOrNull { force ->
        when {
            force.conditions != null -> allConditionsAreMatched(force.conditions, context)
            force.segments != null -> allGroupSegmentsAreMatched(
                force.segments,
                context,
                datafileReader
            )

            else -> false
        }
    }
}

internal fun FeaturevisorInstance.getMatchedTraffic(
    traffic: List<Traffic>,
    context: Context,
    datafileReader: DatafileReader,
): Traffic? {

    return traffic.firstOrNull { trafficItem ->
        allGroupSegmentsAreMatched(trafficItem.segments, context, datafileReader)
    }
}

internal fun FeaturevisorInstance.getMatchedAllocation(
    traffic: Traffic,
    bucketValue: Int,
): Allocation? {

    return traffic.allocation.firstOrNull { allocation ->
        with(allocation.range) {
            bucketValue in this.first()..this.last()
        }
    }
}

internal data class MatchedTrafficAndAllocation(
    val matchedTraffic: Traffic?,
    val matchedAllocation: Allocation?,
)

internal fun FeaturevisorInstance.getMatchedTrafficAndAllocation(
    traffic: List<Traffic>,
    context: Context,
    bucketValue: Int,
    datafileReader: DatafileReader,
    logger: Logger?,
): MatchedTrafficAndAllocation {

    var matchedAllocation: Allocation? = null
    val matchedTraffic = traffic.firstOrNull { trafficItem ->
        if (allGroupSegmentsAreMatched(trafficItem.segments, context, datafileReader)) {
            matchedAllocation = getMatchedAllocation(trafficItem, bucketValue)
            true
        } else {
            false
        }
    }

    return MatchedTrafficAndAllocation(matchedTraffic, matchedAllocation)
}
