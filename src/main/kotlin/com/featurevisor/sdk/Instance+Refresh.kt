package com.featurevisor.sdk

import com.featurevisor.sdk.FeaturevisorError.*
import com.featurevisor.types.EventName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun FeaturevisorInstance.startRefreshing() = when {
    datafileUrl == null -> {
        logger?.error("cannot start refreshing since `datafileUrl` is not provided")
        throw MissingDatafileUrlWhileRefreshing
    }

    refreshJob != null -> logger?.warn("refreshing has already started")
    refreshInterval == null -> logger?.warn("no `refreshInterval` option provided")
    else -> {
        refreshJob = CoroutineScope(Dispatchers.Unconfined).launch {
            while (isActive) {
                refresh()
                delay(refreshInterval)
            }
        }
    }
}

fun FeaturevisorInstance.stopRefreshing() {
    refreshJob?.cancel()
    refreshJob = null
    logger?.warn("refreshing has stopped")
}

private fun FeaturevisorInstance.refresh() {
    logger?.debug("refreshing datafile")
    when {
        statuses.refreshInProgress -> logger?.warn("refresh in progress, skipping")
        datafileUrl.isNullOrBlank() -> logger?.error("cannot refresh since `datafileUrl` is not provided")
        else -> {
            statuses.refreshInProgress = true
            fetchDatafileContent(
                datafileUrl,
                handleDatafileFetch,
                rawResponseReady,
            ) { result ->

                if (result.isSuccess) {
                    val datafileContent = result.getOrThrow()
                    val currentRevision = getRevision()
                    val newRevision = datafileContent.revision
                    val isNotSameRevision = currentRevision != newRevision

                    datafileReader = DatafileReader(datafileContent)
                    logger?.info("refreshed datafile")

                    emitter.emit(EventName.REFRESH)
                    if (isNotSameRevision) {
                        emitter.emit(EventName.UPDATE)
                    }

                    statuses.refreshInProgress = false
                } else {
                    logger?.error(
                        "failed to refresh datafile",
                        mapOf("error" to result)
                    )
                    statuses.refreshInProgress = false
                }
            }
        }
    }
}
