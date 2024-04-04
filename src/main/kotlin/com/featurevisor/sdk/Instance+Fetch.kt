package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import kotlinx.serialization.decodeFromString
import java.io.IOException
import okhttp3.*
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.lang.IllegalArgumentException

// MARK: - Fetch datafile content
@Throws(IOException::class)
internal fun FeaturevisorInstance.fetchDatafileContent(
    url: String,
    handleDatafileFetch: DatafileFetchHandler? = null,
    completion: (Result<DatafileContent>) -> Unit,
) {
    try {
        handleDatafileFetch?.let { handleFetch ->
            val result = handleFetch(url)
            completion(result)
        } ?: run {
            fetchDatafileContentFromUrl(url, completion)
        }
    }catch (e:Exception){
        completion(Result.failure(FeaturevisorError.InvalidUrl(url)))
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

const val BODY_BYTE_COUNT = 1000000L
private inline fun fetch(
    request: Request,
    crossinline completion: (Result<DatafileContent>) -> Unit,
) {
    val client = OkHttpClient()
    val call = client.newCall(request)
    call.enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.peekBody(BODY_BYTE_COUNT)
            if (response.isSuccessful) {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val responseBodyString = responseBody.string()
                FeaturevisorInstance.companionLogger?.debug(responseBodyString)
                try {
                    val content = json.decodeFromString<DatafileContent>(responseBodyString)
                    completion(Result.success(content))
                } catch(throwable: Throwable) {
                    completion(
                        Result.failure(
                            FeaturevisorError.UnparsableJson(
                                responseBody.string(),
                                response.message
                            )
                        )
                    )
                }
            } else {
                completion(
                    Result.failure(
                        FeaturevisorError.UnparsableJson(
                            responseBody.string(),
                            response.message
                        )
                    )
                )
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            completion(Result.failure(e))
        }
    })
}
