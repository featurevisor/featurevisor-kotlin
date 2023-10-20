package com.featurevisor.types

/**
 * Datafile-only types
 */
// 0 to 100,000
typealias Percentage = Int

data class Range(
    val start: Percentage,
    val end: Percentage,
)

data class Allocation(
    val variation: VariationValue,
    val range: Range,
)

data class Traffic(
    val key: RuleKey,
    val segments: GroupSegment,
    val percentage: Percentage,

    val enabled: Boolean?,
    val variation: VariationValue?,
    val variables: VariableValues?,

    val allocation: List<Allocation>,
)

typealias PlainBucketBy = String

typealias AndBucketBy = List<String>

data class OrBucketBy(
    val or: List<String>,
)

sealed class BucketBy {
    data class Single(val bucketBy: PlainBucketBy) : BucketBy()
    data class And(val bucketBy: AndBucketBy) : BucketBy()
    data class Or(val bucketBy: OrBucketBy) : BucketBy()
}

data class RequiredWithVariation(
    val key: FeatureKey,
    val variation: VariationValue,
)

sealed class Required {
    data class FeatureKey(val required: com.featurevisor.types.FeatureKey) : Required()
    data class WithVariation(val required: RequiredWithVariation) : Required()
}

data class Feature(
    val key: FeatureKey,
    val deprecated: Boolean?,
    val variablesSchema: List<VariableSchema>?,
    val variations: List<Variation>?,
    val bucketBy: BucketBy,
    val required: List<Required>?,
    val traffic: List<Traffic>,
    val force: List<Force>?,

    // if in a Group (mutex), these are available slot ranges
    val ranges: List<Range>?,
)

data class DatafileContent(
    val schemaVersion: String,
    val revision: String,
    val attributes: List<Attribute>,
    val segments: List<Segment>,
    val features: List<Feature>,
)

data class OverrideFeature(
    val enabled: Boolean,
    val variation: VariationValue?,
    val variables: VariableValues?,
)
