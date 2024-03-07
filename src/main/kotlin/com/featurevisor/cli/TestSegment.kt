package com.featurevisor.cli

import com.featurevisor.sdk.segmentIsMatched
import com.featurevisor.types.*

fun testSegment(test: TestSegment, segmentFilePath:String):TestResult{
    val segmentKey = test.key

    val testResult = TestResult(
        type = "segment",
        key = segmentKey,
        notFound = false,
        duration = 0,
        passed = true,
        assertions = mutableListOf()
    )

    test.assertions.forEachIndexed { index, segmentAssertion ->
        val assertions = getSegmentAssertionsFromMatrix(index, segmentAssertion)

        assertions.forEach {

            val testResultAssertion = TestResultAssertion(
                description = it.description.orEmpty(),
                duration = 0,
                passed = true,
                errors = mutableListOf()
            )

            val yamlSegment = parseYamlSegment("$segmentFilePath/segments/$segmentKey.yml")
            val expected = it.expectedToMatch
            val actual = segmentIsMatched(yamlSegment!!, it.context)
            val passed = actual == expected

            if (!passed){
                val testResultAssertionError = TestResultAssertionError(
                    type = "segment",
                    expected = expected,
                    actual = actual
                )

                (testResultAssertion.errors as MutableList<TestResultAssertionError>).add(testResultAssertionError)

                testResult.passed = false
                testResultAssertion.passed = false
            }

            (testResult.assertions as MutableList<TestResultAssertion>).add(testResultAssertion)

        }
    }

    return testResult
}
