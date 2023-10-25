package com.featurevisor.sdk

data class Statuses(var ready: Boolean, var refreshInProgress: Boolean)

internal fun FeaturevisorInstance.isReady(): Boolean {
    return statuses.ready
}
