package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import java.io.IOException
import okhttp3.*
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.lang.IllegalArgumentException

// MARK: - Fetch datafile content
@Throws(IOException::class)
fun FeaturevisorInstance.fetchDatafileContent(
    url: String,
    handleDatafileFetch: DatafileFetchHandler? = null,
    completion: (Result<DatafileContent>) -> Unit,
) {
    handleDatafileFetch?.let { handleFetch ->
        val result = handleFetch(url)
        completion(result)
    } ?: run {
        fetchDatafileContentFromUrl(url, completion)
    }
}

private fun fetchDatafileContentFromUrl(
    url: String,
    completion: (Result<DatafileContent>) -> Unit,
) {
    try {
        val httpUrl = url.toHttpUrl()
        val request = Request.Builder()
            .url(httpUrl)
            .addHeader("Content-Type", "application/json")
            .build()

        fetch(request, completion)
    } catch (throwable: IllegalArgumentException) {
        completion(Result.failure(FeaturevisorError.InvalidUrl(url)))
    }
}

private inline fun <reified T> fetch(
    request: Request,
    crossinline completion: (Result<T>) -> Unit,
) {
    val client = OkHttpClient()
    val call = client.newCall(request)
    call.enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body
            if (response.isSuccessful && responseBody != null) {
                val json = Json { ignoreUnknownKeys = true }
                val content = json.decodeFromString<T>(responseBody.string())
                completion(Result.success(content))
            } else {
                completion(Result.failure(FeaturevisorError.UnparsableJson(responseBody?.string(), response.message)))
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            completion(Result.failure(e))
        }
    })
}
