package com.featurevisor.sdk

import kotlin.test.Test
import kotlin.test.assertEquals

class BucketTest {
    @Test
    fun getBucketedNumberReturnsExpectedValues() {

        val expectedResults =
            mapOf(
                "foo" to 20602,
                "bar" to 89144,
                "123.foo" to 3151,
                "123.bar" to 9710,
                "123.456.foo" to 14432,
                "123.456.bar" to 1982
            )

        for ((key, value) in expectedResults) {
            val result = Bucket.getBucketedNumber(key)

            assertEquals(value, result, "Expected: $value for $key, got: $result")
        }
    }
}
