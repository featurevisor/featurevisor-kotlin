package com.featurevisor.sdk

data class Statuses(var ready: Boolean, var refreshInProgress: Boolean)

fun FeaturevisorInstance.isReady(): Boolean {
    return statuses.ready
}
