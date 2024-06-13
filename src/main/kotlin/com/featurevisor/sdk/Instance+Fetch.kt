package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import com.featurevisor.types.DatafileFetchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

// MARK: - Fetch datafile content
@Throws(IOException::class)
internal fun fetchDatafileContentJob(
    url: String,
    logger: Logger?,
    coroutineScope: CoroutineScope,
    retryCount: Int = 3, // Retry count
    retryInterval: Long = 300L, // Retry interval in milliseconds
    handleDatafileFetch: DatafileFetchHandler? = null,
    completion: (Result<DatafileFetchResult>) -> Unit,
): Job {
    val job = Job()
    coroutineScope.launch(job) {
        fetchDatafileContent(
            url = url,
            handleDatafileFetch = handleDatafileFetch,
            completion = completion,
            retryCount = retryCount,
            retryInterval = retryInterval,
            job = job,
            logger = logger,
        )
    }
    return job
}

internal suspend fun fetchDatafileContent(
    url: String,
    logger: Logger? = null,
    retryCount: Int = 1,
    retryInterval: Long = 0L,
    job: Job? = null,
    handleDatafileFetch: DatafileFetchHandler? = null,
    completion: (Result<DatafileFetchResult>) -> Unit,
) {
    handleDatafileFetch?.let { handleFetch ->
        for (attempt in 0 until retryCount) {
            if (job != null && (job.isCancelled || job.isActive.not())) {
                completion(Result.failure(FeaturevisorError.FetchingDataFileCancelled))
                break
            }

            val result = handleFetch(url)
            result.fold(
                onSuccess = {
                    completion(Result.success(DatafileFetchResult(it, "")))
                    return
                },
                onFailure = { exception ->
                    if (attempt < retryCount - 1) {
                        logger?.error(exception.localizedMessage)
                        delay(retryInterval)
                    } else {
                        completion(Result.failure(exception))
                    }
                }
            )
        }
    } ?: run {
        fetchDatafileContentFromUrl(
            url = url,
            completion = completion,
            retryCount = retryCount,
            retryInterval = retryInterval,
            job = job,
            logger = logger,
        )
    }
}

const val BODY_BYTE_COUNT = 1000000L
private val client = OkHttpClient()

private suspend fun fetchDatafileContentFromUrl(
    url: String,
    logger: Logger?,
    retryCount: Int,
    retryInterval: Long,
    job: Job?,
    completion: (Result<DatafileFetchResult>) -> Unit,
) {
    try {
        val httpUrl = url.toHttpUrl()
        val request = Request.Builder()
            .url(httpUrl)
            .addHeader("Content-Type", "application/json")
            .build()

        fetchWithRetry(
            request = request,
            completion = completion,
            retryCount = retryCount,
            retryInterval = retryInterval,
            job = job,
            logger = logger,
        )
    } catch (throwable: IllegalArgumentException) {
        completion(Result.failure(FeaturevisorError.InvalidUrl(url)))
    } catch (e: Exception) {
        logger?.error("Exception occurred during datafile fetch: ${e.message}")
        completion(Result.failure(e))
    }
}

private suspend fun fetchWithRetry(
    request: Request,
    logger: Logger?,
    completion: (Result<DatafileFetchResult>) -> Unit,
    retryCount: Int,
    retryInterval: Long,
    job: Job?
) {
    for (attempt in 0 until retryCount) {
        if (job != null && (job.isCancelled || job.isActive.not())) {
            completion(Result.failure(FeaturevisorError.FetchingDataFileCancelled))
            return
        }

        val call = client.newCall(request)
        try {
            val response = call.execute()
            val responseBody = response.peekBody(BODY_BYTE_COUNT)
            val responseBodyString = responseBody.string()
            if (response.isSuccessful) {
                val json = Json { ignoreUnknownKeys = true }
                FeaturevisorInstance.companionLogger?.debug(responseBodyString)
                val content = json.decodeFromString<DatafileContent>(responseBodyString)
                completion(Result.success(DatafileFetchResult(content, responseBodyString)))
                return
            } else {
                if (attempt < retryCount - 1) {
                    logger?.error("Request failed with message: ${response.message}")
                    delay(retryInterval)
                } else {
                    completion(Result.failure(FeaturevisorError.UnparsableJson(responseBodyString, response.message)))
                }
            }
        } catch (e: IOException) {
            val isInternetException = e is ConnectException || e is UnknownHostException
            if (attempt >= retryCount - 1 || isInternetException) {
                completion(Result.failure(e))
            } else {
                logger?.error("IOException occurred during request: ${e.message}")
                delay(retryInterval)
            }
        } catch (e: Exception) {
            logger?.error("Exception occurred during request: ${e.message}")
            completion(Result.failure(e))
        }
    }
}
