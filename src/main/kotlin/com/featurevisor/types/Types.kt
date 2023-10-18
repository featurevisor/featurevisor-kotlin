package com.featurevisor.types

typealias Context = Map<AttributeKey, AttributeValue>

typealias VariationValue = String

typealias VariableKey = String

enum class VariableType(val value: String) {
    BOOLEAN("boolean"),
    STRING("string"),
    INTEGER("integer"),
    DOUBLE("double"),
    ARRAY("array"),
    OBJECT("object"),
    JSON("json");
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

typealias StickyFeatures = Map<FeatureKey, OverrideFeature>

typealias InitialFeatures = Map<FeatureKey, OverrideFeature>

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
