package com.featurevisor.testRunner

import com.featurevisor.sdk.FeaturevisorInstance
import com.featurevisor.sdk.getVariable
import com.featurevisor.sdk.getVariation
import com.featurevisor.sdk.isEnabled
import com.featurevisor.types.*

data class BenchmarkOutput(
    val value: Any? = null,
    val duration: Double
)

data class BenchMarkOptions(
    val environment: String = "",
    val feature: String = "",
    val n: Int = 0,
    val projectRootPath: String = "",
    val context: Context = emptyMap(),
    val variation: Boolean? = null,
    val variable: String? = null,
)

fun benchmarkFeature(option: BenchMarkOptions) {
    println("Running benchmark for feature ${option.feature}...")

    println("Building datafile containing all features for ${option.environment}...")

    val datafileBuildStart = System.nanoTime().toDouble()

    val datafileContent = buildDataFileAsPerEnvironment(option.projectRootPath, option.environment)

    val datafileBuildEnd = System.nanoTime().toDouble()

    val datafileBuildDuration = datafileBuildEnd - datafileBuildStart

    println("Datafile build duration: ${convertNanoSecondToMilliSecond(datafileBuildDuration)}")

    val sdk = initializeSdkWithDataFileContent(datafileContent)

    println("...SDK initialized")

    println("Against context: ${option.context}")

    val output: BenchmarkOutput

    if (option.variable != null) {
        println("Evaluating variable ${option.variable} ${option.n} times...")

        output = benchmarkFeatureVariable(
            sdk,
            feature = option.feature,
            variableKey = option.variable,
            context = option.context,
            n = option.n
        )

    } else if (option.variation != null) {
        println("Evaluating variation ${option.variation} ${option.n} times...")

        output = benchmarkFeatureVariation(
            sdk,
            feature = option.feature,
            context = option.context,
            n = option.n
        )
    } else {
        println("Evaluating flag ${option.n} times...")

        output = benchmarkFeatureFlag(
            sdk,
            feature = option.feature,
            context = option.context,
            n = option.n
        )
    }

    println("Evaluated value : ${output.value}")
    println("Total duration  : ${convertNanoSecondToMilliSecond(output.duration)}")
    if (option.n != 0) {
        println("Average duration: ${convertNanoSecondToMilliSecond(output.duration / option.n)}")
    }
}


fun benchmarkFeatureFlag(
    f: FeaturevisorInstance,
    feature: FeatureKey,
    context: Context,
    n: Int
): BenchmarkOutput {
    val start = System.nanoTime().toDouble()
    var value: Any = false

    for (i in 0..n) {
        value = f.isEnabled(featureKey = feature, context = context)
    }

    val end = System.nanoTime().toDouble()

    return BenchmarkOutput(
        value = value,
        duration = end - start
    )
}


fun benchmarkFeatureVariation(
    f: FeaturevisorInstance,
    feature: FeatureKey,
    context: Context,
    n: Int
): BenchmarkOutput {
    val start = System.nanoTime().toDouble()
    var value: VariationValue? = null

    for (i in 0..n) {
        value = f.getVariation(featureKey = feature, context = context)
    }

    val end = System.nanoTime().toDouble()

    return BenchmarkOutput(
        value = value,
        duration = end - start
    )
}

fun benchmarkFeatureVariable(
    f: FeaturevisorInstance,
    feature: FeatureKey,
    variableKey: VariableKey,
    context: Context,
    n: Int
): BenchmarkOutput {
    val start = System.nanoTime().toDouble()
    var value: VariableValue? = null

    for (i in 0..n) {
        value = f.getVariable(featureKey = feature, variableKey = variableKey, context = context)
    }

    val end = System.nanoTime().toDouble()

    return BenchmarkOutput(
        value = value,
        duration = end - start
    )
}
