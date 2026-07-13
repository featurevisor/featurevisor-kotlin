package com.featurevisor.types

import com.featurevisor.sdk.serializers.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import java.util.*

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
    val segments: GroupSegment? = null,
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

typealias AttributeKey = String

@Serializable
data class Attribute(
    val key: AttributeKey,
    val type: String,
    val archived: Boolean? = null,
    val capture: Boolean? = null,
)

sealed class AttributeValue {
    data class StringValue(val value: String?) : AttributeValue()
    data class IntValue(val value: Int) : AttributeValue()
    data class DoubleValue(val value: Double) : AttributeValue()
    data class BooleanValue(val value: Boolean) : AttributeValue()
    data class DateValue(val value: Date) : AttributeValue()
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

@Serializable(with = ConditionValueSerializer::class)
sealed class ConditionValue {
    data class StringValue(val value: String?) : ConditionValue()
    data class IntValue(val value: Int) : ConditionValue()
    data class DoubleValue(val value: Double) : ConditionValue()
    data class BooleanValue(val value: Boolean) : ConditionValue()
    data class ArrayValue(val values: List<String>) : ConditionValue()
    data class DateTimeValue(val value: Date) : ConditionValue()
}

typealias SegmentKey = String

@Serializable
data class Segment(
    val archived: Boolean? = null,
    val key: SegmentKey,

    @SerialName("conditions")
    val conditionStrings: String,

    @Transient
    var conditions: Condition? = null,
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

typealias Range = List<Int>

@Serializable
data class Allocation(
    val variation: VariationValue,
    val range: Range,
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

@Serializable(with = RequiredSerializer::class)
sealed class Required {
    data class FeatureKey(val required: com.featurevisor.types.FeatureKey) : Required()
    data class WithVariation(val required: RequiredWithVariation) : Required()
}

@Serializable
data class Feature(
    val key: FeatureKey,
    val deprecated: Boolean? = null,

    @SerialName("variablesSchema")
    val variablesSchemaString: JsonElement? = null,

    @SerialName("variations")
    val variationStrings: JsonElement? = null,

    @SerialName("bucketBy")
    val bucketByString: JsonElement,

    @SerialName("traffic")
    val trafficString: JsonElement,

    @SerialName("force")
    val forceString: JsonElement? = null,

    val required: List<Required>? = null,
    val ranges: List<Range>? = null,

    @Transient
    @SerialName("variablesSchemaObject")
    var variablesSchema: List<VariableSchema>? = null,

    @Transient
    @SerialName("variationsObject")
    var variations: List<Variation>? = null,

    @Transient
    @SerialName("bucketByObject")
    var bucketBy: BucketBy? = null,

    @Transient
    @SerialName("trafficObject")
    var traffic: List<Traffic>? = null,

    @Transient
    @SerialName("forceObject")
    var force: List<Force>? = null
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

/**
 * Tests
 */

typealias AssertionMatrix = Map<String, List<AttributeValue>>


data class FeatureAssertion(
    var description: String? = null,
    var environment: EnvironmentKey = "staging",
    // bucket weight: 0 to 100
    var at: WeightType = WeightType.IntType(40),
    var context: Context = mapOf("devMode" to AttributeValue.BooleanValue(false)),
    val expectedToBeEnabled: Boolean? = null,
    val expectedVariation: VariationValue? = null,
    val expectedVariables: VariableValues? = null,
    val matrix: AssertionMatrix? = null
)

data class TestFeature(
    val key: FeatureKey,
    val assertions: List<FeatureAssertion>,
)

data class SegmentAssertion(
    var description: String? = null,
    var context: Context,
    val expectedToMatch: Boolean,
    val matrix: AssertionMatrix? = null
)

data class TestSegment(
    val key: SegmentKey,
    val assertions: List<SegmentAssertion>,
)

sealed class Test {
    data class Feature(val value: TestFeature) : Test()
    data class Segment(val value: TestSegment) : Test()
}

sealed class WeightType {
    data class IntType(val value: Int) : WeightType()

    data class DoubleType(val value: Double) : WeightType()

    data class StringType(val value: String) : WeightType()
}

data class TestResultAssertionError(
    val type: String,
    val expected: Any? = null,
    val actual: Any? = null,
    val message: String? = null,
    val details: Map<String, Any>? = null
)

data class TestResultAssertion(
    val description: String,
    val environment: EnvironmentKey? = null,
    var duration: Long,
    var passed: Boolean,
    val errors: List<TestResultAssertionError>?
)

data class TestResult(
    val type: String,
    val key: FeatureKey,
    var notFound: Boolean? = null,
    var passed: Boolean,
    var duration: Long,
    val assertions: List<TestResultAssertion>
)

data class ExecutionResult(
    var passed: Boolean,
    val assertionsCount: AssertionsCount
)

data class AssertionsCount(
    var passed: Int = 0,
    var failed: Int = 0
)

@Serializable
data class Configuration(
    val environments: List<String>,
    val tags: List<String>,
    val defaultBucketBy: String,
    val prettyState: Boolean,
    val prettyDatafile: Boolean,
    val stringify: Boolean,
    val featuresDirectoryPath: String,
    val segmentsDirectoryPath: String,
    val attributesDirectoryPath: String,
    val groupsDirectoryPath: String,
    val testsDirectoryPath: String,
    val stateDirectoryPath: String,
    val outputDirectoryPath: String,
    val siteExportDirectoryPath: String
)
