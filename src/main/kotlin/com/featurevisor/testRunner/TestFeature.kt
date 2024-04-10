package com.featurevisor.testRunner

import com.featurevisor.sdk.Logger
import com.featurevisor.sdk.getVariable
import com.featurevisor.sdk.getVariation
import com.featurevisor.sdk.isEnabled
import com.featurevisor.types.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

fun testFeature(
    testFeature: TestFeature,
    datafileContentByEnvironment:MutableMap<String, DatafileContent>,
    option: TestProjectOption
): TestResult {
    val testStartTime = System.currentTimeMillis()
    val featureKey = testFeature.key

    val testResult = TestResult(
        type = "feature",
        key = featureKey,
        notFound = false,
        duration = 0,
        passed = true,
        assertions = mutableListOf()
    )

    testFeature.assertions.forEachIndexed { index, assertion ->
        val assertions = getFeatureAssertionsFromMatrix(index, assertion)

        assertions.forEach {
            val assertionStartTime = System.currentTimeMillis()

            val testResultAssertion = TestResultAssertion(
                description = it.description.orEmpty(),
                environment = it.environment,
                duration = 0,
                passed = true,
                errors = mutableListOf()
            )

            if (option.assertionPattern.isNotEmpty() && !it.description.orEmpty().contains(option.assertionPattern)) {
                return@forEach
            }

            val datafileContent = datafileContentByEnvironment[it.environment]
                ?: getDataFileContent(
                    featureName = testFeature.key,
                    environment = it.environment,
                    projectRootPath = option.projectRootPath.orEmpty()
                )


//                if (option.fast) {
//                if (it.environment.equals("staging", true)) dataFile.stagingDataFiles else dataFile.productionDataFiles
//            } else {
//                getDataFileContent(
//                    featureName = testFeature.key,
//                    environment = it.environment,
//                    projectRootPath = option.projectRootPath.orEmpty()
//                )
//            }

            if (option.showDatafile) {
                printNormalMessage("")
                printNormalMessage(datafileContent.toString())
                printNormalMessage("")
            }

            if (datafileContent != null) {

                val sdk = getSdkInstance(datafileContent, it)

                if (option.verbose) {
                    sdk.setLogLevels(
                        listOf(
                            Logger.LogLevel.DEBUG,
                            Logger.LogLevel.INFO,
                            Logger.LogLevel.WARN,
                            Logger.LogLevel.ERROR
                        )
                    )
                }

                if (testFeature.key.isEmpty()) {
                    testResult.notFound = true
                    testResult.passed = false

                    return testResult
                }

                // isEnabled
                if (it.expectedToBeEnabled != null) {
                    val isEnabled = sdk.isEnabled(testFeature.key, it.context)

                    if (isEnabled != it.expectedToBeEnabled) {
                        testResult.passed = false
                        testResultAssertion.passed = false

                        (testResultAssertion.errors as MutableList<TestResultAssertionError>).add(
                            TestResultAssertionError(
                                type = "flag",
                                expected = it.expectedToBeEnabled,
                                actual = isEnabled
                            )
                        )
                    }
                }

                //Variation
                if (!it.expectedVariation.isNullOrEmpty()) {
                    val variation = sdk.getVariation(testFeature.key, it.context)

                    if (variation != it.expectedVariation) {
                        testResult.passed = false
                        testResultAssertion.passed = false

                        (testResultAssertion.errors as MutableList<TestResultAssertionError>).add(
                            TestResultAssertionError(
                                type = "variation",
                                expected = it.expectedVariation,
                                actual = variation
                            )
                        )
                    }
                }

                //Variables
                if (assertion.expectedVariables is Map<*, *>) {
                    assertion.expectedVariables.forEach { (variableKey, expectedValue) ->
                        val actualValue = sdk.getVariable(featureKey, variableKey, it.context)
                        val passed: Boolean

                        val variableSchema = datafileContent.features.find { feature ->
                            feature.key == testFeature.key
                        }?.variablesSchema?.find { variableSchema ->
                            variableSchema.key.equals(variableKey, ignoreCase = true)
                        }

                        if (variableSchema == null) {
                            testResult.passed = false
                            testResultAssertion.passed = false

                            (testResultAssertion.errors as MutableList<TestResultAssertionError>).add(
                                TestResultAssertionError(
                                    type = "variable",
                                    expected = it.expectedVariation,
                                    actual = null,
                                    message = "schema for variable \"${variableKey}\" not found in feature"
                                )
                            )
                            return@forEach
                        }

                        if (variableSchema.type == VariableType.JSON) {
                            // JSON type
                            val parsedExpectedValue = if (expectedValue is VariableValue.StringValue) {
                                try {
                                    Json.decodeFromString<Map<String, JsonElement>>(expectedValue.value)
                                } catch (e: Exception) {
                                    expectedValue
                                }
                            } else {
                                expectedValue
                            }

                            passed = when (actualValue) {
                                is VariableValue.ArrayValue -> checkIfArraysAreEqual(
                                    stringToArray(parsedExpectedValue.toString()).orEmpty().toTypedArray(),
                                    actualValue.values.toTypedArray()
                                )

                                is VariableValue.ObjectValue -> checkIfObjectsAreEqual(
                                    (parsedExpectedValue as VariableValue.ObjectValue).value,
                                    (actualValue as VariableValue.ObjectValue).value
                                )

                                is VariableValue.JsonValue -> checkJsonIsEquals(
                                    (expectedValue as VariableValue.JsonValue).value,
                                    (actualValue as VariableValue.JsonValue).value
                                )

                                else -> parsedExpectedValue == actualValue
                            }

                            if (!passed) {
                                testResult.passed = false
                                testResultAssertion.passed = false

                                val expectedValueString =
                                    if (expectedValue !is VariableValue.StringValue) expectedValue.toString() else expectedValue
                                val actualValueString =
                                    if (actualValue !is VariableValue.StringValue) actualValue.toString() else actualValue

                                (testResultAssertion.errors as MutableList<TestResultAssertionError>).add(
                                    TestResultAssertionError(
                                        type = "variable",
                                        expected = expectedValueString,
                                        actual = actualValueString,
                                        details = mapOf("variableKey" to variableKey)
                                    )
                                )
                            }
                        } else {
                            passed = when (expectedValue) {
                                is VariableValue.ArrayValue -> checkIfArraysAreEqual(
                                    (expectedValue as VariableValue.ArrayValue).values.toTypedArray(),
                                    (actualValue as VariableValue.ArrayValue).values.toTypedArray()
                                )

                                is VariableValue.ObjectValue -> checkIfObjectsAreEqual(expectedValue, actualValue)
                                else -> expectedValue == actualValue
                            }

                            if (!passed) {
                                testResult.passed = false
                                testResultAssertion.passed = false

                                val expectedValueString =
                                    if (expectedValue !is VariableValue.StringValue) expectedValue.toString() else expectedValue
                                val actualValueString =
                                    if (actualValue !is VariableValue.StringValue) actualValue.toString() else actualValue

                                (testResultAssertion.errors as MutableList<TestResultAssertionError>).add(
                                    TestResultAssertionError(
                                        type = "variable",
                                        expected = expectedValueString,
                                        actual = actualValueString,
                                        details = mapOf("variableKey" to variableKey)
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                testResult.passed = false
                testResultAssertion.passed = false

                (testResultAssertion.errors as MutableList<TestResultAssertionError>).add(
                    TestResultAssertionError(
                        type = "Data File",
                        expected = null,
                        actual = null,
                        message = "Unable to generate Data File"
                    )
                )
            }
            testResultAssertion.duration = System.currentTimeMillis() - assertionStartTime
            (testResult.assertions as MutableList<TestResultAssertion>).add(testResultAssertion)
        }
    }
    testResult.duration = System.currentTimeMillis() - testStartTime
    return testResult
}
