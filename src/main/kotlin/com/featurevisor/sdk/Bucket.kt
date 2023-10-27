package com.featurevisor.sdk

import com.goncalossilva.murmurhash.MurmurHash3
import kotlin.math.floor

object Bucket {

    private const val HASH_SEED = 1u
    private const val MAX_HASH_VALUE = 4294967296 // 2^32
    // 100% * 1000 to include three decimal places in the same integer value
    private const val MAX_BUCKETED_NUMBER = 100000

    fun getBucketedNumber(bucketKey: String): Int {
        val hashValue = MurmurHash3(HASH_SEED).hash32x86(bucketKey.toByteArray())
        val ratio = hashValue.toDouble() / MAX_HASH_VALUE

        return floor(ratio * MAX_BUCKETED_NUMBER).toInt()
    }
}
