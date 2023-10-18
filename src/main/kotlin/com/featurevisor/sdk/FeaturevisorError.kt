package com.featurevisor.sdk

sealed class FeaturevisorError(message: String, result: String? = null) : Throwable(message = message) {
    object MissingDatafileOptions : FeaturevisorError("Missing data file options")
    class FetchingDataFileFailed(result: String) : FeaturevisorError("Fetching data file failed", result)
}
