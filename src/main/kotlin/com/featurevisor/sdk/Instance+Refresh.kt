package com.featurevisor.sdk

import com.featurevisor.types.EventName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun FeaturevisorInstance.refresh() {
    logger?.debug("refreshing datafile")

    if (statuses.refreshInProgress) {
        logger?.warn("refresh in progress, skipping")
        return
    }

    if (datafileUrl == null) {
        logger?.error("cannot refresh since `datafileUrl` is not provided")
        return
    }

    statuses.refreshInProgress = true

    fetchDatafileContent(
        datafileUrl,
        handleDatafileFetch,
    ) { result ->
        val self = this // To capture the instance in a closure
        if (self == null) {
            return@fetchDatafileContent
        }

        if (result.isSuccess) {
            val datafileContent = result.getOrThrow()
            val currentRevision = self.getRevision()
            val newRevision = datafileContent.revision
            val isNotSameRevision = currentRevision != newRevision

            self.datafileReader = DatafileReader(datafileContent)
            logger?.info("refreshed datafile")

            self.emitter.emit(EventName.REFRESH)

            if (isNotSameRevision) {
                self.emitter.emit(EventName.UPDATE)
            }

            self.statuses.refreshInProgress = false
        } else {
            self.logger?.error("failed to refresh datafile", mapOf("error" to result))
            self.statuses.refreshInProgress = false
        }
    }
}

fun FeaturevisorInstance.startRefreshing() {
    val datafileUrl = datafileUrl
    if (datafileUrl == null) {
        logger?.error("cannot start refreshing since `datafileUrl` is not provided")
        return
    }

    if (refreshJob != null) {
        logger?.warn("refreshing has already started")
        return
    }

    if (refreshInterval == null) {
        logger?.warn("no `refreshInterval` option provided")
        return
    }

    refreshJob = CoroutineScope(Dispatchers.Unconfined).launch {
        while (isActive) {
            refresh()
            delay(refreshInterval.toLong())
        }
    }
}

fun FeaturevisorInstance.stopRefreshing() {
    refreshJob?.cancel()
    refreshJob = null

    logger?.warn("refreshing has stopped")
}
