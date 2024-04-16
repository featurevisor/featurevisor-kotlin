package com.featurevisor.sdk

public sealed class FeaturevisorError(message: String) : Throwable(message = message) {

    /// Thrown when attempting to init Featurevisor instance without passing datafile and datafileUrl.
    /// At least one of them is required to init the SDK correctly
    public object MissingDatafileOptions : FeaturevisorError("Missing data file options")

    public class FetchingDataFileFailed(public val result: String) : FeaturevisorError("Fetching data file failed")

    /// Thrown when receiving unparseable Datafile JSON responses.
    /// - Parameters:
    ///   - data: The data being parsed.
    ///   - errorMessage: The message from the error which occured during parsing.
    public class UnparsableJson(public val data: String?, errorMessage: String) : FeaturevisorError(errorMessage)

    /// Thrown when attempting to construct an invalid URL.
    /// - Parameter string: The invalid URL string.
    public class InvalidUrl(public val url: String?) : FeaturevisorError("Invalid URL")

    public object MissingDatafileUrlWhileRefreshing : FeaturevisorError("Missing datafile url need to refresh")
}
