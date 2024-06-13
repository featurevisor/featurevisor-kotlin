package com.featurevisor.sdk

sealed class FeaturevisorError(message: String) : Throwable(message = message) {

    /// Thrown when attempting to init Featurevisor instance without passing datafile and datafileUrl.
    /// At least one of them is required to init the SDK correctly
    object MissingDatafileOptions : FeaturevisorError("Missing data file options")

    class FetchingDataFileFailed(val result: String) : FeaturevisorError("Fetching data file failed")

    /// Thrown when receiving unparseable Datafile JSON responses.
    /// - Parameters:
    ///   - data: The data being parsed.
    ///   - errorMessage: The message from the error which occured during parsing.
    class UnparsableJson(val data: String?, errorMessage: String) : FeaturevisorError(errorMessage)

    /// Thrown when attempting to construct an invalid URL.
    /// - Parameter string: The invalid URL string.
    class InvalidUrl(val url: String?) : FeaturevisorError("Invalid URL")

    object MissingDatafileUrlWhileRefreshing : FeaturevisorError("Missing datafile url need to refresh")

    /// Fetching was cancelled
    object FetchingDataFileCancelled : FeaturevisorError("Fetching data file cancelled")
}
