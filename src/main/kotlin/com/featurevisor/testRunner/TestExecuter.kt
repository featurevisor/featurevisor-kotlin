package com.featurevisor.testRunner

import com.featurevisor.types.*
import java.io.File

data class TestProjectOption(
    val keyPattern: String = "",
    val assertionPattern: String = "",
    val verbose: Boolean = false,
    val showDatafile: Boolean = false,
    val onlyFailures: Boolean = false,
    val fast: Boolean = false,
    val testDirPath: String = "tests",
    val projectRootPath: String = getRootProjectDir()
)

fun startTest(option: TestProjectOption) {
    var hasError = false
    val folder = File("${option.projectRootPath}/${option.testDirPath}")
    val listOfFiles = folder.listFiles()
    var executionResult: ExecutionResult? = null
    val startTime = System.currentTimeMillis()
    var passedTestsCount = 0
    var failedTestsCount = 0
    var passedAssertionsCount = 0
    var failedAssertionsCount = 0

    if (!listOfFiles.isNullOrEmpty()) {
        val datafile =
            if (option.fast) buildDataFileForBothEnvironments(projectRootPath = option.projectRootPath) else DataFile(
                null,
                null
            )
        if (option.fast && (datafile.stagingDataFiles == null || datafile.productionDataFiles == null)) {
            return
        }
        for (file in listOfFiles) {
            if (file.isFile) {
                if (file.extension.equals("yml", true)) {
                    val filePath = file.absoluteFile.path
                    try {
                        executionResult = executeTest(filePath, dataFile = datafile, option)
                    } catch (e: Exception) {
                        printMessageInRedColor("Exception in $filePath --> ${e.message}")
                    }

                    if (executionResult == null) {
                        return
                    }

                    if (executionResult.passed) {
                        passedTestsCount++
                    } else {
                        hasError = true
                        failedTestsCount++
                    }

                    passedAssertionsCount += executionResult.assertionsCount.passed
                    failedAssertionsCount += executionResult.assertionsCount.failed
                } else {
                    printMessageInRedColor("The file is not valid yml file")
                }
            }
        }

        val endTime = System.currentTimeMillis() - startTime

        if (!option.onlyFailures || hasError) {
            printNormalMessage("\n----")
        }
        printNormalMessage("")

        if (hasError) {
            printMessageInRedColor("\n\nTest specs: $passedTestsCount passed, $failedTestsCount failed")
            printMessageInRedColor("Test Assertion: $passedAssertionsCount passed, $failedAssertionsCount failed")
        } else {
            printMessageInGreenColor("\n\nTest specs: $passedTestsCount passed, $failedTestsCount failed")
            printMessageInGreenColor("Test Assertion: $passedAssertionsCount passed, $failedAssertionsCount failed")
        }
        printBoldMessage("Time:       ${prettyDuration(endTime)}")
    } else {
        printMessageInRedColor("Directory is Empty or not exists")
    }
}

private fun executeTest(filePath: String, dataFile: DataFile, option: TestProjectOption): ExecutionResult {
    val test = parseTestFeatureAssertions(filePath)

    val executionResult = ExecutionResult(
        passed = true,
        assertionsCount = AssertionsCount(0, 0)
    )

    test?.let {
        val key = when (test) {
            is Test.Feature -> test.value.key
            is Test.Segment -> test.value.key
        }

        if (option.keyPattern.isNotEmpty() && !key.contains(option.keyPattern)) {
            return@let
        }

        val testResult: TestResult = when (test) {
            is Test.Feature -> {
                testFeature(test.value, dataFile = dataFile, option)
            }

            is Test.Segment -> {
                testSegment(test.value, option.projectRootPath)
            }
        }

        if (!option.onlyFailures) {
            printTestResult(testResult)
        } else {
            if (!testResult.passed) {
                printTestResult(testResult)
            }
        }

        if (!testResult.passed) {
            executionResult.passed = false

            executionResult.assertionsCount.failed = testResult.assertions.count { !it.passed }
            executionResult.assertionsCount.passed += testResult.assertions.size - executionResult.assertionsCount.failed
        } else {
            executionResult.assertionsCount.passed = testResult.assertions.size
        }
    }
    return executionResult
}

fun getDataFileContent(featureName: String, environment: String, projectRootPath: String) =
    try {
        getJsonForFeatureUsingCommand(
            featureName = featureName,
            environment = environment,
            projectRootPath = projectRootPath
        )?.run {
            convertToDataClass<DatafileContent>()
        }
    } catch (e: Exception) {
        printMessageInRedColor("Exception while parsing data file --> ${e.message}")
        null
    }





