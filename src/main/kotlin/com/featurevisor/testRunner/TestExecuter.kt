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
    val projectRootPath: String? = null
)

fun startTest(option: TestProjectOption) {
    option.projectRootPath?.let {
        val configurations =  parseConfiguration(option.projectRootPath)
        var hasError = false
        val folder = File(configurations.testsDirectoryPath)
        val listOfFiles = folder.listFiles()?.sortedBy { it }
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
                            executionResult = executeTest(filePath, dataFile = datafile, option, configurations)
                        } catch (e: Exception) {
                            printMessageInRedColor("Exception in $filePath --> ${e.message}")
                        }

                        if (executionResult == null) {
                            continue
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
    } ?: printNormalMessage("Root Project Path Not Found")

}

private fun executeTest(filePath: String, dataFile: DataFile, option: TestProjectOption,configuration: Configuration): ExecutionResult? {
    val test = parseTestFeatureAssertions(filePath)

    val executionResult = ExecutionResult(
        passed = true,
        assertionsCount = AssertionsCount(0, 0)
    )

    if (test != null){
        val key = when (test) {
            is Test.Feature -> test.value.key
            is Test.Segment -> test.value.key
        }

        if (option.keyPattern.isNotEmpty() && !key.contains(option.keyPattern)) {
            return null
        }

        val testResult: TestResult = when (test) {
            is Test.Feature -> {
                testFeature(
                    testFeature = test.value,
                    dataFile = dataFile,
                    option = option
                )
            }

            is Test.Segment -> {
                testSegment(
                    test.value,
                    configuration
                )
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






