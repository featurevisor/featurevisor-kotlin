package com.featurevisor.testRunner

import com.featurevisor.sdk.segmentIsMatched
import com.featurevisor.types.TestResult
import com.featurevisor.types.TestResultAssertion
import com.featurevisor.types.TestResultAssertionError
import com.featurevisor.types.TestSegment

fun testSegment(test: TestSegment, segmentFilePath: String): TestResult {
    val testStartTime = System.currentTimeMillis()
    val segmentKey = test.key

    val testResult = TestResult(
        type = "segment",
        key = segmentKey,
        notFound = false,
        duration = 0,
        passed = true,
        assertions = mutableListOf()
    )

    for (aIndex in 0 until test.assertions.size) {
        val assertions = getSegmentAssertionsFromMatrix(aIndex, test.assertions[aIndex])

        for (assertion in assertions) {
            val assertionStartTime = System.currentTimeMillis()

            val testResultAssertion = TestResultAssertion(
                description = assertion.description.orEmpty(),
                duration = 0,
                passed = true,
                errors = mutableListOf()
            )

            val yamlSegment = parseYamlSegment("$segmentFilePath/segments/$segmentKey.yml")
            val expected = assertion.expectedToMatch
            val actual = segmentIsMatched(yamlSegment!!, assertion.context)
            val passed = actual == expected

            if (!passed) {
                val testResultAssertionError = TestResultAssertionError(
                    type = "segment",
                    expected = expected,
                    actual = actual
                )

                (testResultAssertion.errors as MutableList<TestResultAssertionError>).add(testResultAssertionError)
                testResult.passed = false
                testResultAssertion.passed = false
            }

            testResultAssertion.duration = System.currentTimeMillis() - assertionStartTime
            (testResult.assertions as MutableList<TestResultAssertion>).add(testResultAssertion)
        }
    }

    testResult.duration = System.currentTimeMillis() - testStartTime
    return testResult
}
