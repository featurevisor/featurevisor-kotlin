@file:JvmName("TestExecuter")
package com.featurevisor.cli

import com.featurevisor.sdk.evaluateFlag
import com.featurevisor.sdk.getVariation
import com.featurevisor.types.Spec
import com.featurevisor.types.DatafileContent
import java.io.File


fun main(args: Array<String>){
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
            startTest(rootPathInParam,testDirInParam)
        }
    }
}

internal fun startTest(projectRootPath: String= "",testDirPath: String= "") {
    val rootPath = projectRootPath.ifEmpty {
        getRootProjectDir()
    }
    val testDir = testDirPath.ifEmpty {
        "tests"
    }
    getAllFilesInDirectory(rootPath,testDir)
}

internal fun getAllFilesInDirectory(projectRootPath: String,testDirPath: String) {
    val folder = File("$projectRootPath/$testDirPath")
    val listOfFiles = folder.listFiles()
    var noOfSpecPassed = 0
    if (!listOfFiles.isNullOrEmpty()){
        for (file in listOfFiles) {
            if (file.isFile) {
                if(file.extension == "yml"){
                    val filePath = file.absoluteFile.path
                    try {
                        val isAllTestPassed = testAssertion(filePath, projectRootPath)
                        if (isAllTestPassed) {
                            noOfSpecPassed++
                        }
                    } catch (e: Exception) {
                        printMessageInRedColor("Exception in $filePath --> ${e.message}")
                    }
                }else{
                    printMessageInRedColor("The file is not valid yml file")
                }
            }
        }
        printMessageInGreenColor("Test specs: $noOfSpecPassed passed, ${listOfFiles.size - noOfSpecPassed} failed")
    }else{
        printMessageInRedColor("Directory is Empty or not exists")
    }
}

private fun testAssertion(filePath: String, projectRootPath: String):Boolean {
    val assertionFileData = parseYamlAssertions(filePath)
    return if (assertionFileData != null) {
        checkAssertions(assertionFileData, projectRootPath)
    }else{
        false
    }
}

internal fun testFeature(testDirPath: String, projectRootPath: String) {
    val assertionTypes = parseYamlAssertions("$projectRootPath/tests$testDirPath")
    if (assertionTypes != null) {
        val isAllAssertionPassed = checkAssertions(assertionTypes, projectRootPath)
        if (isAllAssertionPassed) {
            println("Spec Passed 1 Failed 0")
        }else{
            println("Spec Passed 0 Failed 1")
        }
    }
}

private fun checkAssertions(spec: Spec?, projectRootPath: String): Boolean {
    var noOfTestPassedAssertionCounter = 0
    val noOfTotalAssertions = spec?.assertion?.size ?: 0
    val featureNameWithExt = if (!spec?.feature.isNullOrEmpty()) "${spec?.feature}.feature.yml" else "${spec?.segment}.spec.yml"
    val featureNameWithoutExt = if (!spec?.feature.isNullOrEmpty()) "feature \"${spec?.feature}\"" else "segment \"${spec?.segment}\""
    printNormalMessage("Testing: $featureNameWithExt")
    printNormalMessage("\t$featureNameWithoutExt:")
    spec?.assertion?.forEachIndexed { index, assertion ->

        if (!spec.feature.isNullOrEmpty()) {

            val datafileContent = getDataFileContent(
                featureName = spec.feature,
                environment = assertion.environment.orEmpty(),
                projectRootPath = projectRootPath
            )

            datafileContent?.let {
                val featurevisorInstance = getSdkInstance(datafileContent, assertion)

                val evaluation = featurevisorInstance.evaluateFlag(spec.feature, assertion.context)

                val variation = featurevisorInstance.getVariation(spec.feature, assertion.context)

                val isVariableAssertedSuccessfully = assertion.expectedVariables?.run {
                    assertVariables(spec.feature, assertion, featurevisorInstance)?.all { it }
                }

                val isExpectedToBeEnabledAssertedSuccessfully = assertExpectedToBeEnabled(evaluation.enabled, assertion.expectedToBeEnabled)

                val isVariationAssertedSuccessfully = assertVariation(variation, assertion.expectedVariation)

                if (assertion.expectedVariables.isNullOrEmpty() && isExpectedToBeEnabledAssertedSuccessfully) {
                    noOfTestPassedAssertionCounter++
                    printAssertionSuccessfulMessage("Assertion #${index + 1}: (${assertion.environment}) ${assertion.description}")
                } else if (isExpectedToBeEnabledAssertedSuccessfully == isVariableAssertedSuccessfully == isVariationAssertedSuccessfully) {
                    noOfTestPassedAssertionCounter++
                    printAssertionSuccessfulMessage("Assertion #${index + 1}: (${assertion.environment}) ${assertion.description}")
                } else {
                    printAssertionFailedMessage("Assertion #${index + 1}: (${assertion.environment}) ${assertion.description}")
                }
            }
        } else if (!spec.segment.isNullOrEmpty()){
            val isSegmentAssertedSuccessfully = assertSegment(projectRootPath,assertion,spec.segment)
            if (isSegmentAssertedSuccessfully){
                noOfTestPassedAssertionCounter++
                printAssertionSuccessfulMessage("Assertion #${index + 1}: (${assertion.environment}) ${assertion.description}")
            }else{
                printAssertionFailedMessage("Assertion #${index + 1}: (${assertion.environment}) ${assertion.description}")
            }
        }
    }
    printMessageInGreenColor("\n\nAssertions: $noOfTestPassedAssertionCounter passed, ${noOfTotalAssertions - noOfTestPassedAssertionCounter} failed\n")
    return noOfTotalAssertions == noOfTestPassedAssertionCounter
}

private fun getDataFileContent(featureName: String, environment: String, projectRootPath: String) =
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





