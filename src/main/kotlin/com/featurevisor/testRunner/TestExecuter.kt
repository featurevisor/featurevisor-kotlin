@file:JvmName("TestExecuter")

package com.featurevisor.testRunner

import com.featurevisor.types.*
import java.io.File

fun main(args: Array<String>) {
    when (args.size) {
        0 -> {
            startTest()
        }

        1 -> {
            val rootPathInParam = args[0]
            startTest(rootPathInParam)
        }

        else -> {
            val rootPathInParam = args[0]
            val testDirInParam = args[1]
            startTest(rootPathInParam, testDirInParam)
        }
    }
}

internal fun startTest(projectRootPath: String = "", testDirPath: String = "") {
    val rootPath = projectRootPath.ifEmpty {
        getRootProjectDir()
    }
    val testDir = testDirPath.ifEmpty {
        "tests"
    }
    getAllFilesInDirectory(rootPath, testDir)
}

internal fun getAllFilesInDirectory(projectRootPath: String, testDirPath: String) {
    val folder = File("$projectRootPath/$testDirPath")
    val listOfFiles = folder.listFiles()
    var executionResult: ExecutionResult? = null

    var passedTestsCount = 0
    var failedTestsCount = 0

    var passedAssertionsCount = 0
    var failedAssertionsCount = 0

    if (!listOfFiles.isNullOrEmpty()) {
        for (file in listOfFiles) {
            if (file.isFile) {
                if (file.extension.equals("yml", true)) {
                    val filePath = file.absoluteFile.path
                    try {
                        executionResult = testAssertion(filePath, projectRootPath)
                    } catch (e: Exception) {
                        printMessageInRedColor("Exception in $filePath --> ${e.message}")
                    }


                    if (executionResult?.passed == true) {
                        passedTestsCount++
                    } else {
                        failedTestsCount++
                    }

                    passedAssertionsCount += executionResult?.assertionsCount?.passed ?: 0
                    failedAssertionsCount += executionResult?.assertionsCount?.failed ?: 0

                } else {
                    printMessageInRedColor("The file is not valid yml file")
                }
            }
        }
        printMessageInGreenColor("Test specs: $passedTestsCount passed, $failedTestsCount failed")
        printMessageInGreenColor("Test Assertion: $passedAssertionsCount passed, $failedAssertionsCount failed")
    } else {
        printMessageInRedColor("Directory is Empty or not exists")
    }
}

fun testSingleFeature(featureKey: String, projectRootPath: String) {
    val test = parseTestFeatureAssertions("$projectRootPath/tests/$featureKey.spec.yml")

    val executionResult = ExecutionResult(
        passed = false,
        assertionsCount = AssertionsCount(0, 0)
    )

    if (test == null) {
        println("No File available")
        return
    }

    val testResult = testFeature(testFeature = (test as Test.Feature).value, projectRootPath)

    printTestResult(testResult)

    if (!testResult.passed) {
        executionResult.passed = false

        executionResult.assertionsCount.failed = testResult.assertions.count { !it.passed }
        executionResult.assertionsCount.passed += testResult.assertions.size - executionResult.assertionsCount.failed
    } else {
        executionResult.assertionsCount.passed = testResult.assertions.size
    }

    printMessageInGreenColor("Test Assertion: ${executionResult.assertionsCount.passed} passed, ${executionResult.assertionsCount.failed} failed")
}

fun testSingleSegment(featureKey: String, projectRootPath: String) {
    val test = parseTestFeatureAssertions("$projectRootPath/tests/$featureKey.segment.yml")

    val executionResult = ExecutionResult(
        passed = false,
        assertionsCount = AssertionsCount(0, 0)
    )

    val testResult = testSegment(test = (test as Test.Segment).value, projectRootPath)

    printTestResult(testResult)

    if (!testResult.passed) {
        executionResult.passed = false

        executionResult.assertionsCount.failed = testResult.assertions.count { !it.passed }
        executionResult.assertionsCount.passed += testResult.assertions.size - executionResult.assertionsCount.failed
    } else {
        executionResult.assertionsCount.passed = testResult.assertions.size
    }

    printMessageInGreenColor("Test Assertion: ${executionResult.assertionsCount.passed} passed, ${executionResult.assertionsCount.failed} failed")
}

private fun testAssertion(filePath: String, projectRootPath: String): ExecutionResult {
    val test = parseTestFeatureAssertions(filePath)

    val executionResult = ExecutionResult(
        passed = true,
        assertionsCount = AssertionsCount(0, 0)
    )

    test?.let {
        val testResult: TestResult = when (test) {
            is Test.Feature -> {
                testFeature(test.value, projectRootPath)
            }

            is Test.Segment -> {
                testSegment(test.value, projectRootPath)
            }
        }

        printTestResult(testResult)

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





