package com.featurevisor.sdk

import com.featurevisor.sdk.FeaturevisorError.MissingDatafileUrlWhileRefreshing
import com.featurevisor.types.EventName
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
        refreshJob = coroutineScope.launch {
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

private suspend fun FeaturevisorInstance.refresh() {
    logger?.debug("refreshing datafile")
    when {
        statuses.refreshInProgress -> logger?.warn("refresh in progress, skipping")
        datafileUrl.isNullOrBlank() -> logger?.error("cannot refresh since `datafileUrl` is not provided")
        else -> {
            statuses.refreshInProgress = true
            fetchDatafileContent(
                url = datafileUrl,
                handleDatafileFetch = handleDatafileFetch,
            ) { result ->
                result.onSuccess { datafileContent ->
                    val currentRevision = getRevision()
                    val newRevision = datafileContent.first.revision
                    val isNotSameRevision = currentRevision != newRevision

                    datafileReader = DatafileReader(datafileContent.first)
                    logger?.info("refreshed datafile")

                    emitter.emit(EventName.REFRESH)
                    if (isNotSameRevision) {
                        emitter.emit(EventName.UPDATE)
                    }

                    statuses.refreshInProgress = false
                }.onFailure {
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
