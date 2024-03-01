package com.featurevisor.types

import com.featurevisor.sdk.serializers.BucketBySerializer
import com.featurevisor.sdk.serializers.ConditionSerializer
import com.featurevisor.sdk.serializers.ConditionValueSerializer
import com.featurevisor.sdk.serializers.GroupSegmentSerializer
import com.featurevisor.sdk.serializers.VariableValueSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

typealias Context = Map<AttributeKey, AttributeValue>
typealias VariationValue = String
typealias VariableKey = String

@Serializable
enum class VariableType {
    @SerialName("boolean")
    BOOLEAN,
    @SerialName("string")
    STRING,
    @SerialName("integer")
    INTEGER,
    @SerialName("double")
    DOUBLE,
    @SerialName("array")
    ARRAY,
    @SerialName("object")
    OBJECT,
    @SerialName("json")
    JSON
}

typealias VariableObjectValue = Map<String, VariableValue>

@Serializable(with = VariableValueSerializer::class)
sealed class VariableValue {
    data class BooleanValue(val value: Boolean) : VariableValue()
    data class StringValue(val value: String) : VariableValue()
    data class IntValue(val value: Int) : VariableValue()
    data class DoubleValue(val value: Double) : VariableValue()
    data class ArrayValue(val values: List<String>) : VariableValue()
    data class ObjectValue(val value: VariableObjectValue) : VariableValue()
    data class JsonValue(val value: String) : VariableValue()
}

@Serializable
data class VariableOverride(
    val value: VariableValue,

    // one of the below must be present in YAML
    val conditions: Condition? = null,
    val segments: GroupSegment?=null,
)

@Serializable
data class Variable(
    val key: VariableKey,
    val value: VariableValue,
    val overrides: List<VariableOverride>? = null,
)

@Serializable
data class Variation(
    // only available in YAML
    val description: String? = null,

    val value: VariationValue,

    // 0 to 100 (available from parsed YAML, but not in datafile)
    val weight: Double? = null,

    val variables: List<Variable>? = null,
)

@Serializable
data class VariableSchema(
    val key: VariableKey,
    val type: VariableType,
    val defaultValue: VariableValue,
)

typealias FeatureKey = String

typealias VariableValues = Map<VariableKey, VariableValue>

@Serializable
data class Force(
    // one of the below must be present in YAML
    val conditions: Condition? = null,
    val segments: GroupSegment? = null,

    val enabled: Boolean? = null,
    val variation: VariationValue? = null,
    val variables: VariableValues? = null,
)

data class Slot(
    // @TODO: allow false?
    val feature: FeatureKey? = null,

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

    val enabled: Boolean? = null,
    val variation: VariationValue? = null,
    val variables: VariableValues? = null,
)

data class Environment(
    val expose: Boolean? = null,
    val rules: List<Rule>,
    val force: List<Force>? = null,
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

typealias AttributeKey = String

@Serializable
data class Attribute(
    val key: AttributeKey,
    val type: String,
    val archived: Boolean? = null,
    val capture: Boolean? = null,
)

sealed class AttributeValue {
    data class StringValue(val value: String) : AttributeValue()
    data class IntValue(val value: Int) : AttributeValue()
    data class DoubleValue(val value: Double) : AttributeValue()
    data class BooleanValue(val value: Boolean) : AttributeValue()
    data class DateValue(val value: LocalDate) : AttributeValue()
}

@Serializable(with = ConditionSerializer::class)
sealed class Condition {
    data class Plain(
        val attributeKey: AttributeKey,
        val operator: Operator,
        val value: ConditionValue,
    ) : Condition()

    data class And(val and: List<Condition>) : Condition()
    data class Or(val or: List<Condition>) : Condition()
    data class Not(val not: List<Condition>) : Condition()
}

const val TAG = "FeaturevisorService"

@Serializable(with = ConditionValueSerializer::class)
sealed class ConditionValue {
    data class StringValue(val value: String) : ConditionValue()
    data class IntValue(val value: Int) : ConditionValue()
    data class DoubleValue(val value: Double) : ConditionValue()
    data class BooleanValue(val value: Boolean) : ConditionValue()
    data class ArrayValue(val values: List<String>) : ConditionValue()
    data class DateTimeValue(val value: LocalDate) : ConditionValue()
}

typealias SegmentKey = String

@Serializable
data class Segment(
    val archived: Boolean? = null,
    val key: SegmentKey,
    val conditions: Condition,
)

data class AndGroupSegment(
    val and: List<GroupSegment>,
)

data class OrGroupSegment(
    val or: List<GroupSegment>,
)

data class NotGroupSegment(
    val not: List<GroupSegment>,
)

@Serializable(with = GroupSegmentSerializer::class)
sealed class GroupSegment {
    data class Plain(val segment: SegmentKey) : GroupSegment()
    data class Multiple(val segments: List<GroupSegment>) : GroupSegment()
    data class And(val segment: AndGroupSegment) : GroupSegment()
    data class Or(val segment: OrGroupSegment) : GroupSegment()
    data class Not(val segment: NotGroupSegment) : GroupSegment()
}

enum class Operator(val value: String) {
    EQUALS("equals"),
    NOT_EQUALS("notEquals"),

    // numeric
    GREATER_THAN("greaterThan"),
    GREATER_THAN_OR_EQUALS("greaterThanOrEquals"),
    LESS_THAN("lessThan"),
    LESS_THAN_OR_EQUALS("lessThanOrEquals"),

    // string
    CONTAINS("contains"),
    NOT_CONTAINS("notContains"),
    STARTS_WITH("startsWith"),
    ENDS_WITH("endsWith"),

    // semver (string)
    SEMVER_EQUALS("semverEquals"),
    SEMVER_NOT_EQUALS("semverNotEquals"),
    SEMVER_GREATER_THAN("semverGreaterThan"),
    SEMVER_GREATER_THAN_OR_EQUALS("semverGreaterThanOrEquals"),
    SEMVER_LESS_THAN("semverLessThan"),
    SEMVER_LESS_THAN_OR_EQUALS("semverLessThanOrEquals"),

    // date comparisons
    BEFORE("before"),
    AFTER("after"),

    // array of strings
    IN_ARRAY("in"),
    NOT_IN_ARRAY("notIn");
}

enum class EventName {
    READY,
    REFRESH,
    UPDATE,
    ACTIVATION,
    ERROR,
}

/**
 * Datafile-only types
 */
// 0 to 100,000
typealias Percentage = Int

@Serializable
data class Range(
    val start: Percentage,
    val end: Percentage,
)

@Serializable
data class Allocation(
    val variation: VariationValue,
    val range: List<Int>,
)

@Serializable
data class Traffic(
    val key: RuleKey,
    val segments: GroupSegment,
    val percentage: Percentage,

    val enabled: Boolean? = null,
    val variation: VariationValue? = null,
    val variables: VariableValues? = null,

    val allocation: List<Allocation>,
)

@Serializable(with = BucketBySerializer::class)
sealed class BucketBy {
    data class Single(val bucketBy: String) : BucketBy()
    data class And(val bucketBy: List<String>) : BucketBy()
    data class Or(val bucketBy: List<String>) : BucketBy()
}

data class RequiredWithVariation(
    val key: FeatureKey,
    val variation: VariationValue,
)

@Serializable
sealed class Required {
    data class FeatureKey(val required: com.featurevisor.types.FeatureKey) : Required()
    data class WithVariation(val required: RequiredWithVariation) : Required()
}

@Serializable
data class Feature(
    val key: FeatureKey,
    val deprecated: Boolean? = null,
    val variablesSchema: List<VariableSchema>? = null,
    val variations: List<Variation>? = null,
    val bucketBy: BucketBy,
    val required: List<Required>? = null,
    val traffic: List<Traffic>,
    val force: List<Force>? = null,

    // if in a Group (mutex), these are available slot ranges
    val ranges: List<Range>? = null,
)

@Serializable
data class DatafileContent(
    val schemaVersion: String,
    val revision: String,
    val attributes: List<Attribute>,
    val segments: List<Segment>,
    val features: List<Feature>,
)

@Serializable
data class OverrideFeature(
    val enabled: Boolean,
    val variation: VariationValue? = null,
    val variables: VariableValues? = null,
)

/*
* For Assertions used by test Runner
* */

data class Spec(
    val feature: String? = null,
    val segment: String? = null,
    val assertion: List<Assertion>? = null,
)

data class Assertion(
    val description: String? = null,
    val environment: String? = null,
    val at: Double? = null,
    val context: Context,
    val expectedToBeEnabled: Boolean? = null,
    val expectedVariables: Map<String, Any?>? = null,
    val expectedToMatch: Boolean? = null,
    val expectedVariation: String? = null,
)

data class TestResultAssertionError(
    val type: String,
    val expected: Any?,
    val actual: Any?,
    val message: String?,
    val details: Map<String, Any>?
)

data class TestResultAssertion(
    val description: String,
    val duration: Long,
    val passed: Boolean,
    val errors: List<TestResultAssertionError>?
)

data class TestResult(
    val type: Test,
    val key: String,
    val notFound: Boolean?,
    val passed: Boolean,
    val duration: Long,
    val assertions: List<TestResultAssertion>
)
