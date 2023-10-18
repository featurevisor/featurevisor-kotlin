package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import com.featurevisor.types.InitialFeatures
import com.featurevisor.types.StickyFeatures

typealias Listener = () -> Unit

data class InstanceOptions(
    val bucketKeySeparator: String = defaultBucketKeySeparator,
    val configureBucketKey: ConfigureBucketKey? = null,
    val configureBucketValue: ConfigureBucketValue? = null,
    val datafile: DatafileContent? = null,
    val datafileUrl: String? = null,
    val handleDatafileFetch: DatafileFetchHandler? = null,
    val initialFeatures: InitialFeatures? = null,
    val interceptContext: InterceptContext? = null,
    val logger: Logger? = null,
    val onActivation: Listener? = null,
    val onReady: Listener? = null,
    val onRefresh: Listener? = null,
    val onUpdate: Listener? = null,
    val refreshInterval: Double? = null, // seconds
    val stickyFeatures: StickyFeatures? = null,
) {
    companion object {
        private const val defaultBucketKeySeparator = "."

        // An instance of `InstanceOptions` with sane defaults. This is a singleton instance.
        val default: InstanceOptions = InstanceOptions()
    }
}
