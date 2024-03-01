package com.featurevisor.cli

import com.featurevisor.sdk.FeaturevisorInstance
import com.featurevisor.sdk.InstanceOptions
import com.featurevisor.types.Assertion
import com.featurevisor.types.DatafileContent
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File


internal const val tick = "\u2713"
internal const val cross = "\u2717"

internal const val ANSI_RESET = "\u001B[0m"
internal const val ANSI_RED = "\u001B[31m"
internal const val ANSI_GREEN = "\u001B[32m"

internal const val MAX_BUCKETED_NUMBER = 100000

internal val json = Json {
    ignoreUnknownKeys = true
}

internal fun printMessageInGreenColor(message:String) =
    println("$ANSI_GREEN$message$ANSI_RESET")

internal fun printMessageInRedColor(message: String) =
    println("$ANSI_RED$message$ANSI_RESET")

internal fun printAssertionSuccessfulMessage(message: String) = println("\t$tick $message")


internal fun printAssertionFailedMessage(message: String) =
    println("$ANSI_RED\t$cross $message $ANSI_RESET")

internal fun printNormalMessage(message: String) = println(message)

internal fun getSdkInstance(datafileContent: DatafileContent?, assertion: Assertion) =
    FeaturevisorInstance.createInstance(
        InstanceOptions(
            datafile = datafileContent,
            configureBucketValue = { _, _, _ ->
                ((assertion.at ?: 0.0) * (MAX_BUCKETED_NUMBER / 100)).toInt()
            }
        )
    )

 internal fun getFileForSpecificPath(path: String) = File(path)

 internal inline fun <reified R : Any> String.convertToDataClass() = json.decodeFromString<R>(this)

internal fun getRootProjectDir(): String {
    var currentDir = File("").absoluteFile
    while (currentDir.parentFile != null) {
        if (File(currentDir, "build.gradle.kts").exists()) {
            return currentDir.absolutePath
        }
        currentDir = currentDir.parentFile
    }
    throw IllegalStateException("Root project directory not found.")
}
