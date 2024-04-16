package com.featurevisor.testRunner

import com.featurevisor.sdk.segmentIsMatched
import com.featurevisor.types.TestResult
import com.featurevisor.types.TestResultAssertion
import com.featurevisor.types.TestResultAssertionError
import com.featurevisor.types.TestSegment

internal fun testSegment(testSegment: TestSegment, option: TestProjectOption): TestResult {
    val testStartTime = System.currentTimeMillis()
    val segmentKey = testSegment.key

    val testResult = TestResult(
        type = "segment",
        key = segmentKey,
        notFound = false,
        duration = 0,
        passed = true,
        assertions = mutableListOf()
    )

    testSegment.assertions.forEachIndexed { index, segmentAssertion ->
        val assertions = getSegmentAssertionsFromMatrix(index,segmentAssertion)

        assertions.forEach {
            val assertionStartTime = System.currentTimeMillis()

            val testResultAssertion = TestResultAssertion(
                description = it.description.orEmpty(),
                duration = 0,
                passed = true,
                errors = mutableListOf()
            )

            if (option.assertionPattern.isNotEmpty() && !it.description.orEmpty().contains(option.assertionPattern)) {
                return@forEach
            }

            val yamlSegment = parseYamlSegment("${option.projectRootPath}/segments/$segmentKey.yml")
            val expected = it.expectedToMatch
            val actual = segmentIsMatched(yamlSegment!!, it.context)
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
