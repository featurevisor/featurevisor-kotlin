package com.featurevisor.testRunner

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

internal fun getJsonForFeatureUsingCommand(featureName: String, environment: String, projectRootPath: String) =
    try {
        createCommandForSpecificFeature(featureName, environment).runCommand(getFileForSpecificPath(projectRootPath))
    } catch (e: Exception) {
        printMessageInRedColor("Exception in Commandline execution --> ${e.message}")
        null
    }

fun getJsonForDataFile(environment: String, projectRootPath: String) =
    try {
        createCommandAccordingToEnvironment(environment).runCommand(getFileForSpecificPath(projectRootPath))
    } catch (e: Exception) {
        printMessageInRedColor("Exception in Commandline execution --> ${e.message}")
        null
    }

private fun createCommandForSpecificFeature(featureName: String, environment: String) =
    "npx featurevisor build --feature=$featureName --environment=$environment --print --pretty"

private fun createCommandAccordingToEnvironment(environment: String) =
    "npx featurevisor build --environment=$environment --print --pretty"

private fun String.runCommand(workingDir: File): String? =
    try {
        val parts = this.split("\\s".toRegex())
        val process = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        printMessageInRedColor("Exception while executing command -> ${e.message}")
        null
    }

fun createCommandForConfiguration()=
    "npx featurevisor config --print --pretty"

fun getConfigurationJson(projectRootPath: String) =
    try {
        createCommandForConfiguration().runCommand(getFileForSpecificPath(projectRootPath))
    }catch (e:Exception){
        printMessageInRedColor("Exception in createCommandForConfiguration Commandline execution --> ${e.message}")
        null
    }

