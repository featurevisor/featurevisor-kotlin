package com.featurevisor.sdk

import com.featurevisor.sdk.Conditions.allConditionsAreMatched
import com.featurevisor.types.Allocation
import com.featurevisor.types.Context
import com.featurevisor.types.Feature
import com.featurevisor.types.Force
import com.featurevisor.types.Traffic

fun FeaturevisorInstance.getFeatureByKey(featureKey: String): Feature? {
    return try {
        datafileReader.getFeature(featureKey)
    }catch (e:Exception){
        FeaturevisorInstance.companionLogger?.error("Exception in getFeatureByKey() -> $e")
        null
    }
}

fun FeaturevisorInstance.getFeature(featureKey: String): Feature?{
    return try {
        datafileReader.getFeature(featureKey)
    }catch (e:Exception){
        FeaturevisorInstance.companionLogger?.error("Exception in getFeature() -> $e")
        null
    }
}

internal fun FeaturevisorInstance.findForceFromFeature(
    feature: Feature,
    context: Context,
    datafileReader: DatafileReader,
): Force? {
    return try {
        feature.force?.firstOrNull { force ->
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
    }catch (e:Exception){
        FeaturevisorInstance.companionLogger?.error("Exception in findForceFromFeature() -> $e")
        null
    }
}

internal fun FeaturevisorInstance.getMatchedTraffic(
    traffic: List<Traffic>,
    context: Context,
    datafileReader: DatafileReader,
): Traffic? {
    return try {
        traffic.firstOrNull { trafficItem ->
            allGroupSegmentsAreMatched(trafficItem.segments, context, datafileReader)
        }
    }catch (e:Exception){
        FeaturevisorInstance.companionLogger?.error("Exception in getMatchedTraffic() -> $e")
        null
    }
}

internal fun FeaturevisorInstance.getMatchedAllocation(
    traffic: Traffic,
    bucketValue: Int,
): Allocation? {
    return try {
        traffic.allocation.firstOrNull { allocation ->
            with(allocation.range) {
                bucketValue in this.first()..this.last()
            }
        }
    }catch (e:Exception){
        FeaturevisorInstance.companionLogger?.error("Exception in getMatchedAllocation() -> $e")
        null
    }
}

data class MatchedTrafficAndAllocation(
    val matchedTraffic: Traffic?=null,
    val matchedAllocation: Allocation?=null,
)

internal fun FeaturevisorInstance.getMatchedTrafficAndAllocation(
    traffic: List<Traffic>,
    context: Context,
    bucketValue: Int,
    datafileReader: DatafileReader,
    logger: Logger?,
): MatchedTrafficAndAllocation {
   return try {
        var matchedAllocation: Allocation? = null
        val matchedTraffic = traffic.firstOrNull { trafficItem ->
            if (allGroupSegmentsAreMatched(trafficItem.segments, context, datafileReader)) {
                matchedAllocation = getMatchedAllocation(trafficItem, bucketValue)
                true
            } else {
                false
            }
        }
         MatchedTrafficAndAllocation(matchedTraffic, matchedAllocation)
    }catch (e:Exception){
       FeaturevisorInstance.companionLogger?.error("Exception in getMatchedTrafficAndAllocation() -> $e")
       MatchedTrafficAndAllocation()
    }
}
