package com.featurevisor.types

typealias AttributeKey = String
typealias Context = Map<AttributeKey, AttributeValue>

data class Attribute(
    val key: AttributeKey,
    val type: String,
    val archived: Boolean?,
    val capture: Boolean?,
)

data class PlainCondition(
    val attributeKey: AttributeKey,
    val operator: Operator,
    val value: AttributeValue,
)

data class AndCondition(
    val and: List<Condition>,
)

data class OrCondition(
    val or: List<Condition>,
)

data class NotCondition(
    val not: List<Condition>,
)

sealed class Condition {
    data class Plain(val condition: PlainCondition) : Condition()
    data class Multiple(val conditions: List<Condition>) : Condition()

    data class And(val condition: AndCondition) : Condition()
    data class Or(val condition: OrCondition) : Condition()
    data class Not(val condition: NotCondition) : Condition()
}

typealias SegmentKey = String

data class Segment(
    val archived: Boolean?,
    val key: SegmentKey,
    val conditions: Condition,
)

typealias PlainGroupSegment = SegmentKey

data class AndGroupSegment(
    val and: List<GroupSegment>,
)

data class OrGroupSegment(
    val or: List<GroupSegment>,
)

data class NotGroupSegment(
    val not: List<GroupSegment>,
)

sealed class GroupSegment {
    data class Plain(val segment: PlainGroupSegment) : GroupSegment()
    data class Multiple(val segments: List<GroupSegment>) : GroupSegment()

    data class And(val segment: AndGroupSegment) : GroupSegment()
    data class Or(val segment: OrGroupSegment) : GroupSegment()
    data class Not(val segment: NotGroupSegment) : GroupSegment()
}

typealias VariationValue = String

typealias VariableKey = String

enum class VariableType(val value: String) {
    boolean("boolean"),
    string("string"),
    integer("integer"),
    double("double"),
    array("array"),
    object_("object"),
    json("json");
}

typealias VariableObjectValue = Map<String, VariableValue>

sealed class VariableValue {
    data class BooleanValue(val value: Boolean) : VariableValue()
    data class StringValue(val value: String) : VariableValue()
    data class IntValue(val value: Int) : VariableValue()
    data class DoubleValue(val value: Double) : VariableValue()
    data class ArrayValue(val values: List<String>) : VariableValue()
    data class ObjectValue(val value: VariableObjectValue) : VariableValue()
    data class JsonValue(val value: String) : VariableValue()
}

data class VariableOverride(
    val value: VariableValue,

    // one of the below must be present in YAML
    val conditions: Condition?,
    val segments: GroupSegment?,
)

data class Variable(
    val key: VariableKey,
    val value: VariableValue,
    val overrides: List<VariableOverride>?,
)

data class Variation(
    // only available in YAML
    val description: String?,

    val value: VariationValue,

    // 0 to 100 (available from parsed YAML, but not in datafile)
    val weight: Double?,

    val variables: List<Variable>?,
)

data class VariableSchema(
    val key: VariableKey,
    val type: VariableType,
    val defaultValue: VariableValue,
)

typealias FeatureKey = String

typealias VariableValues = Map<VariableKey, VariableValue>

data class Force(
    // one of the below must be present in YAML
    val conditions: Condition?,
    val segments: GroupSegment?,

    val enabled: Boolean?,
    val variation: VariationValue?,
    val variables: VariableValues?,
)

data class Slot(
    // @TODO: allow false?
    val feature: FeatureKey?,

    // 0 to 100
    val percentage: Weight,
)

data class Group(
    val key: String,
    val description: String,
    val slots: List<Slot>,
)

typealias BucketKey = String

// 0 to 100,000
typealias BucketValue = Int

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

typealias AndBucketBy = List<BucketBy>

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
    data class FeatureKey(val required: FeatureKey) : Required()
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

typealias StickyFeatures = Map<FeatureKey, OverrideFeature>

typealias InitialFeatures = Map<FeatureKey, VariationValue>

/**
 * YAML-only type
 */
// 0 to 100
typealias Weight = Double

typealias EnvironmentKey = String

typealias RuleKey = String

data class Rule(
    val key: RuleKey,
    val segments: GroupSegment,
    val percentage: Weight,

    val enabled: Boolean?,
    val variation: VariationValue?,
    val variables: VariableValues?,
)

data class Environment(
    val expose: Boolean?,
    val rules: List<Rule>,
    val force: List<Force>?,
)

typealias Environments = Map<EnvironmentKey, Environment>

data class ParsedFeature(
    val key: FeatureKey,

    val archived: Boolean?,
    val deprecated: Boolean?,

    val description: String,
    val tags: List<String>,

    val bucketBy: BucketBy,

    val required: List<Required>?,

    val variablesSchema: List<VariableSchema>?,
    val variations: List<Variation>?,

    val environments: Environments,
)

/**
 * Tests
 */
data class FeatureAssertion(
    val description: String?,
    val environment: EnvironmentKey,
    // bucket weight: 0 to 100
    val at: Weight,
    val context: Context,
    val expectedToBeEnabled: Boolean,
    val expectedVariation: VariationValue?,
    val expectedVariables: VariableValues?,
)

data class TestFeature(
    val key: FeatureKey,
    val assertions: List<FeatureAssertion>,
)

data class SegmentAssertion(
    val description: String?,
    val context: Context,
    val expectedToMatch: Boolean,
)

data class TestSegment(
    val key: SegmentKey,
    val assertions: List<SegmentAssertion>,
)

sealed class Test {
    data class Feature(val value: TestFeature) : Test()
    data class Segment(val value: TestSegment) : Test()
}
