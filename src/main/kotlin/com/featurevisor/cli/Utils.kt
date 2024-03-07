package com.featurevisor.cli

import com.featurevisor.sdk.FeaturevisorInstance
import com.featurevisor.sdk.InstanceOptions
import com.featurevisor.types.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.util.Date


internal const val tick = "\u2713"
internal const val cross = "\u2717"

internal const val ANSI_RESET = "\u001B[0m"
internal const val ANSI_RED = "\u001B[31m"
internal const val ANSI_GREEN = "\u001B[32m"

internal const val MAX_BUCKETED_NUMBER = 100000

internal val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

internal fun printMessageInGreenColor(message:String) =
    println("$ANSI_GREEN$message$ANSI_RESET")

internal fun printMessageInRedColor(message: String) =
    println("$ANSI_RED$message$ANSI_RESET")

internal fun printAssertionSuccessfulMessage(message: String) = println("\t$tick $message")


internal fun printAssertionFailedMessage(message: String) =
    println("$ANSI_RED\t$cross $message $ANSI_RESET")

internal fun printNormalMessage(message: String) = println(message)

internal fun getSdkInstance(datafileContent: DatafileContent?, assertion: FeatureAssertion) =
    FeaturevisorInstance.createInstance(
        InstanceOptions(
            datafile = datafileContent,
            configureBucketValue = { _, _, _ ->
                when(assertion.at){
                    is WeightType.IntType -> ((assertion.at as WeightType.IntType).value   * (MAX_BUCKETED_NUMBER / 100))
                    is WeightType.DoubleType -> ((assertion.at as WeightType.DoubleType).value  * (MAX_BUCKETED_NUMBER / 100)).toInt()
                    else -> (MAX_BUCKETED_NUMBER / 100)
                }
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


fun printTestResult(testResult: TestResult) {
    println("")

    printNormalMessage("Testing: ${testResult.key}")

    if (testResult.notFound == true) {
        println(ANSI_RED + "  => ${testResult.type} ${testResult.key} not found"+ ANSI_RED)
        return
    }

    printNormalMessage("  ${testResult.type} \"${testResult.key}\":")

    testResult.assertions.forEachIndexed { index, assertion ->
        if (assertion.passed) {
            printAssertionSuccessfulMessage(assertion.description)
        } else {
            printAssertionFailedMessage(assertion.description)

            assertion.errors?.forEach { error ->
                when {
                    error.message != null -> {
                        printMessageInRedColor("    => ${error.message}")
                    }
                    error.type == "variable" -> {
                        val variableKey = (error.details as Map<*, *>)["variableKey"]

                        printMessageInRedColor("    => variable key: $variableKey")
                        printMessageInRedColor("       => expected: ${error.expected}")
                        printMessageInRedColor("       => received: ${error.actual}")
                    }
                    else -> {
                        printMessageInRedColor(
                            "    => ${error.type}: expected \"${error.expected}\", received \"${error.actual}\""
                        )
                    }
                }
            }
        }
    }
}

fun getContextValue(contextValue: Any?) =
    when(contextValue){
        is Boolean -> AttributeValue.BooleanValue(contextValue)
        is Int -> AttributeValue.IntValue(contextValue)
        is Double -> AttributeValue.DoubleValue(contextValue)
        is String -> AttributeValue.StringValue(contextValue)
        is Date -> AttributeValue.DateValue(contextValue)

        else -> throw Exception("Unsupported context value")
    }

fun getContextValues(contextValue: AttributeValue) =
    when(contextValue){
        is AttributeValue.IntValue -> contextValue.value
        is AttributeValue.DoubleValue -> contextValue.value
        is AttributeValue.StringValue -> contextValue.value
        is AttributeValue.BooleanValue ->contextValue.value
        is AttributeValue.DateValue -> contextValue.value
    }



fun checkIfArraysAreEqual(a: Array<Any>, b: Array<Any>): Boolean {
    if (a.size != b.size) return false

    for (i in a.indices) {
        if (a[i] != b[i]) {
            return false
        }
    }
    return true
}

fun checkIfObjectsAreEqual(a: Any?, b: Any?): Boolean {
    if (a === b) {
        return true
    }

    if (a !is Map<*, *> || b !is Map<*, *>) {
        return false
    }

    val keysA = a.keys
    val keysB = b.keys

    if (keysA.size != keysB.size || !keysA.containsAll(keysB)) {
        return false
    }

    for (key in keysA) {
        val valueA = a[key]
        val valueB = b[key]

        if (!checkIfObjectsAreEqual(valueA, valueB)) {
            return false
        }
    }

    return true
}

fun stringToArray(input: String): List<Any>? {
    if (input.trim().startsWith("[") && input.trim().endsWith("]")) {
        val trimmed = input.trim().substring(1, input.length - 1)
        val elements = trimmed.split(",").map { it.trim() }
        return elements.map { element ->
            when {
                element == "true" || element == "false" -> element.toBoolean()
                element.toIntOrNull() != null -> element.toInt()
                element.toDoubleOrNull() != null -> element.toDouble()
                else -> element
            }
        }
    }
    return null
}

fun checkMapIsEquals(a: String, b: String): Boolean {
    val map1 = Json.decodeFromString<Map<String, JsonElement>>(a)
    val map2 = Json.decodeFromString<Map<String, JsonElement>>(b)
    return map1 == map2
}


