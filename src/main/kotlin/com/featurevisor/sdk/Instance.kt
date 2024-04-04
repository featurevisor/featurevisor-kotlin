/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.featurevisor.sdk

import com.featurevisor.sdk.FeaturevisorError.MissingDatafileOptions
import com.featurevisor.types.*
import com.featurevisor.types.EventName.*
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

typealias ConfigureBucketKey = (Feature, Context, BucketKey) -> BucketKey
typealias ConfigureBucketValue = (Feature, Context, BucketValue) -> BucketValue
typealias InterceptContext = (Context) -> Context
typealias DatafileFetchHandler = (datafileUrl: String) -> Result<DatafileContent>

fun main(args: Array<String>) {

    var abc:FeaturevisorInstance? = null
    var isReady:Boolean = false
    abc =  FeaturevisorInstance.createInstance(
        options = InstanceOptions(
            configureBucketKey = null,
            configureBucketValue = null,
            datafileUrl = "https://features.fe.indazn.com/staging/datafile-tag-android.json",
            logger = Logger.createLogger(
                levels =  listOf(
                    Logger.LogLevel.ERROR,
                    Logger.LogLevel.DEBUG,
                    Logger.LogLevel.WARN,
                    Logger.LogLevel.INFO
                ) ,
                handle = { _, message, _ ->
                    println(message)
                }
            ),
//            onReady = {
//                isReady = true
//                println("onReady  $it")
//
//                val value =  abc?.getVariableJSON<CountriesWithStringValueAndSeparateDefault>("live_preroll_ads","live_preroll_ads_custom_tags", getContext())
//                println("Value : $value")
//
//            },
//            onError = {
//                println("onError   $it")
//            }
        )
    )


    val value =  abc?.getVariableJSON<CountriesWithStringValueAndSeparateDefault>("live_preroll_ads","live_preroll_ads_custom_tags", getContext())
    println("Value : $value")


//    if (isReady){
//        val value =  abc.getVariableJSON<Map<String,String>>("live_preroll_ads","live_preroll_watched_duration", getContext())
//        println("Value : $value")
//    }else{
//        println("Not Ready")
//    }

}


private fun getContext(): Map<String, AttributeValue> =
    mapOf(
        "deviceId" to AttributeValue.StringValue("45645"),
        "version_code" to AttributeValue.StringValue(""),
        "app_info_version" to AttributeValue.StringValue(""),
        "Platform" to AttributeValue.StringValue("Android"),
        "BillingCountry" to AttributeValue.StringValue("In"),
        "GeolocatedCountry" to AttributeValue.StringValue("In"),
        "device_os" to AttributeValue.StringValue("Android"),
        "DevMode" to AttributeValue.BooleanValue(false),
        "device_model" to AttributeValue.StringValue("5dfgfcx"),
        "device_language" to AttributeValue.StringValue("en"),
        "user_status" to AttributeValue.StringValue("Active")
    )



class FeaturevisorInstance private constructor(options: InstanceOptions) {

    companion object {
        fun createInstance(options: InstanceOptions): FeaturevisorInstance {
            return FeaturevisorInstance(options)
        }

        var companionLogger: Logger? = null
    }

    private val on: (EventName, Listener) -> Unit
    private val off: (EventName) -> Unit
    private val addListener: (EventName, Listener) -> Unit
    private val removeListener: (EventName) -> Unit
    private val removeAllListeners: () -> Unit

    internal val statuses = Statuses(ready = false, refreshInProgress = false)

    internal val logger = options.logger
    internal val initialFeatures = options.initialFeatures
    internal val interceptContext = options.interceptContext
    internal val emitter: Emitter = Emitter()
    internal val datafileUrl = options.datafileUrl
    internal val handleDatafileFetch = options.handleDatafileFetch
    internal val refreshInterval = options.refreshInterval

    internal lateinit var datafileReader: DatafileReader

    internal var stickyFeatures = options.stickyFeatures
    internal var bucketKeySeparator = options.bucketKeySeparator
    internal var configureBucketKey = options.configureBucketKey
    internal var configureBucketValue = options.configureBucketValue
    internal var refreshJob: Job? = null

    init {
        with(options) {
            companionLogger = logger
            if (onReady != null) {
                emitter.addListener(event = READY, listener = onReady)
            }

            if (onRefresh != null) {
                emitter.addListener(
                    REFRESH, onRefresh
                )
            }
            if (onUpdate != null) {
                emitter.addListener(
                    UPDATE, onUpdate
                )
            }
            if (onActivation != null) {
                emitter.addListener(
                    ACTIVATION, onActivation
                )
            }
            if (onError != null) {
                emitter.addListener(
                    ERROR, onError
                )
            }

            on = emitter::addListener
            off = emitter::removeListener
            addListener = emitter::addListener
            removeListener = emitter::removeListener
            removeAllListeners = emitter::removeAllListeners

            when {
                datafile != null -> {
                    datafileReader = DatafileReader(datafile)
                    statuses.ready = true
                    emitter.emit(READY)
                }

                datafileUrl != null -> {
                    fetchDatafileContent(datafileUrl) { result ->
                        if (result.isSuccess) {
                            datafileReader = DatafileReader(result.getOrThrow())
                            statuses.ready = true
                            emitter.emit(READY, result.getOrThrow())
                            if (refreshInterval != null) startRefreshing()
                        } else {
                            logger?.error("Failed to fetch datafile: $result")
                            emitter.emit(ERROR)
                        }
                    }
                }

                else -> throw MissingDatafileOptions
            }
        }
    }

    fun setLogLevels(levels: List<Logger.LogLevel>) {
        this.logger?.setLevels(levels)
    }

    fun setDatafile(datafileJSON: String) {
        val data = datafileJSON.toByteArray(Charsets.UTF_8)
        try {
            val datafileContent = Json.decodeFromString<DatafileContent>(String(data))
            datafileReader = DatafileReader(datafileJson = datafileContent)
        } catch (e: Exception) {
            logger?.error("could not parse datafile", mapOf("error" to e))
        }
    }

    fun setDatafile(datafileContent: DatafileContent) {
        datafileReader = DatafileReader(datafileJson = datafileContent)
    }

    fun setStickyFeatures(stickyFeatures: StickyFeatures?) {
        this.stickyFeatures = stickyFeatures
    }

    fun getRevision(): String {
        return datafileReader.getRevision()
    }
}
