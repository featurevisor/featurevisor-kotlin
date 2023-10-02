package com.featurevisor.types

typealias AttributeKey = String

sealed class AttributeValue {
    data class StringValue(val value: String) : AttributeValue()
    data class IntValue(val value: Int) : AttributeValue()
    data class DoubleValue(val value: Double) : AttributeValue()
    data class BooleanValue(val value: Boolean) : AttributeValue()

    // @TODO: implement Date
    object NullValue : AttributeValue()
}

typealias Context = Map<AttributeKey, AttributeValue>

data class Attribute(
    val key: AttributeKey,
    val type: String,
    val archived: Boolean?,
    val capture: Boolean?,
)

enum class Operator(val value: String) {
    equals("equals"),
    notEquals("notEquals"),

    // numeric
    greaterThan("greaterThan"),
    greaterThanOrEquals("greaterThanOrEqual"),
    lessThan("lessThan"),
    lessThanOrEquals("lessThanOrEqual"),

    // string
    contains("contains"),
    notContains("notContains"),
    startsWith("startsWith"),
    endsWith("endsWith"),

    // semver (string)
    semverEquals("semverEquals"),
    semverNotEquals("semverNotEquals"),
    semverGreaterThan("semverGreaterThan"),
    semverGreaterThanOrEquals("semverGreaterThanOrEqual"),
    semverLessThan("semverLessThan"),
    semverLessThanOrEquals("semverLessThanOrEqual"),

    // date comparisons
    before("before"),
    after("after"),

    // array of strings
    inArray("in"),
    notInArray("notIn");
}

sealed class ConditionValue {
    data class StringValue(val value: String) : ConditionValue()
    data class IntValue(val value: Int) : ConditionValue()
    data class DoubleValue(val value: Double) : ConditionValue()
    data class BooleanValue(val value: Boolean) : ConditionValue()
    data class ArrayValue(val values: Array<String>) : ConditionValue()

    // @TODO: implement Date
    object NullValue : ConditionValue()
}

data class PlainCondition(
    val attribute: AttributeKey,
    val operator: Operator,
    val value: ConditionValue,
)

data class AndCondition(
    val and: Array<Condition>,
)

data class OrCondition(
    val or: Array<Condition>,
)

data class NotCondition(
    val not: Array<Condition>,
)

sealed class Condition {
    data class Plain(val condition: PlainCondition) : Condition()
    data class Multiple(val conditions: Array<Condition>) : Condition()

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
    val and: Array<GroupSegment>,
)

data class OrGroupSegment(
    val or: Array<GroupSegment>,
)

data class NotGroupSegment(
    val not: Array<GroupSegment>,
)

sealed class GroupSegment {
    data class Plain(val segment: PlainGroupSegment) : GroupSegment()
    data class Multiple(val segments: Array<GroupSegment>) : GroupSegment()

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
    data class ArrayValue(val values: Array<String>) : VariableValue()
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
    val overrides: Array<VariableOverride>?,
)

data class Variation(
    // only available in YAML
    val description: String?,

    val value: VariationValue,

    // 0 to 100 (available from parsed YAML, but not in datafile)
    val weight: Double?,

    val variables: Array<Variable>?,
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
    val slots: Array<Slot>,
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

    val allocation: Array<Allocation>,
)

typealias PlainBucketBy = String

typealias AndBucketBy = Array<BucketBy>

data class OrBucketBy(
    val or: Array<String>,
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
    val variablesSchema: Array<VariableSchema>?,
    val variations: Array<Variation>?,
    val bucketBy: BucketBy,
    val required: Array<Required>?,
    val traffic: Array<Traffic>,
    val force: Array<Force>?,

    // if in a Group (mutex), these are available slot ranges
    val ranges: Array<Range>?,
)

data class DatafileContent(
    val schemaVersion: String,
    val revision: String,
    val attributes: Array<Attribute>,
    val segments: Array<Segment>,
    val features: Array<Feature>,
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
    val rules: Array<Rule>,
    val force: Array<Force>?,
)

typealias Environments = Map<EnvironmentKey, Environment>

data class ParsedFeature(
    val key: FeatureKey,

    val archived: Boolean?,
    val deprecated: Boolean?,

    val description: String,
    val tags: Array<String>,

    val bucketBy: BucketBy,

    val required: Array<Required>?,

    val variablesSchema: Array<VariableSchema>?,
    val variations: Array<Variation>?,

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
    val feature: FeatureKey,
    val assertions: Array<FeatureAssertion>,
)

data class SegmentAssertion(
    val description: String?,
    val context: Context,
    val expectedToMatch: Boolean,
)

data class TestSegment(
    val segment: SegmentKey,
    val assertions: Array<SegmentAssertion>,
)

sealed class Test {
    data class Feature(val value: TestFeature) : Test()
    data class Segment(val value: TestSegment) : Test()
}
