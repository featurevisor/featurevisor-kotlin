package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import com.featurevisor.types.InitialFeatures
import com.featurevisor.types.StickyFeatures

typealias Listener = (Array<out Any>) -> Unit

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
    val onError: Listener? = null,
    val refreshInterval: Long? = null, // seconds
    val stickyFeatures: StickyFeatures? = null,
    val retryInterval: Long = 300L,
    val retryCount: Int = 1,
) {
    companion object {
        private const val defaultBucketKeySeparator = "."
    }
}
