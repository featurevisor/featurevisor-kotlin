package com.featurevisor.cli

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

internal fun getJsonForFeatureUsingCommand(featureName: String, environment: String, projectRootPath: String) =
    try {
        createCommand(featureName, environment).runCommand(getFileForSpecificPath(projectRootPath))
    } catch (e: Exception) {
        printMessageInRedColor("Exception in Commandline execution --> ${e.message}")
        null
    }

private fun createCommand(featureName: String, environment: String) =
    "npx featurevisor build --feature=$featureName --environment=$environment --print --pretty"

private fun String.runCommand(workingDir: File): String? =
    try {
        val parts = this.split("\\s".toRegex())
        val process = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.waitFor(60, TimeUnit.MINUTES)
        process.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        printMessageInRedColor("Exception while executing command -> ${e.message}")
        null
    }
