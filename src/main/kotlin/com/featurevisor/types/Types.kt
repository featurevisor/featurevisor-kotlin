package com.featurevisor.types

import com.featurevisor.sdk.serializers.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

public typealias Context = Map<AttributeKey, AttributeValue>
public typealias VariationValue = String
public typealias VariableKey = String

@Serializable
public enum class VariableType {
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

public typealias VariableObjectValue = Map<String, VariableValue>

@Serializable(with = VariableValueSerializer::class)
public sealed class VariableValue {
    public data class BooleanValue(val value: Boolean) : VariableValue()
    public data class StringValue(val value: String) : VariableValue()
    public data class IntValue(val value: Int) : VariableValue()
    public data class DoubleValue(val value: Double) : VariableValue()
    public data class ArrayValue(val values: List<String>) : VariableValue()
    public data class ObjectValue(val value: VariableObjectValue) : VariableValue()
    public data class JsonValue(val value: String) : VariableValue()
}

@Serializable
public data class VariableOverride(
    val value: VariableValue,

    // one of the below must be present in YAML
    val conditions: Condition? = null,
    val segments: GroupSegment?=null,
)

@Serializable
public data class Variable(
    val key: VariableKey,
    val value: VariableValue,
    val overrides: List<VariableOverride>? = null,
)

@Serializable
public data class Variation(
    // only available in YAML
    val description: String? = null,

    val value: VariationValue,

    // 0 to 100 (available from parsed YAML, but not in datafile)
    val weight: Double? = null,

    val variables: List<Variable>? = null,
)

@Serializable
public data class VariableSchema(
    val key: VariableKey,
    val type: VariableType,
    val defaultValue: VariableValue,
)

public typealias FeatureKey = String

public typealias VariableValues = Map<VariableKey, VariableValue>

@Serializable
public data class Force(
    // one of the below must be present in YAML
    val conditions: Condition? = null,
    val segments: GroupSegment? = null,

    val enabled: Boolean? = null,
    val variation: VariationValue? = null,
    val variables: VariableValues? = null,
)

public data class Slot(
    // @TODO: allow false?
    val feature: FeatureKey? = null,

    // 0 to 100
    val percentage: Weight,
)

public data class Group(
    val key: String,
    val description: String,
    val slots: List<Slot>,
)

public typealias BucketKey = String
// 0 to 100,000
public typealias BucketValue = Int
public typealias StickyFeatures = Map<FeatureKey, OverrideFeature>
public typealias InitialFeatures = Map<FeatureKey, OverrideFeature>

/**
 * YAML-only type
 */
// 0 to 100
internal typealias Weight = Double
internal typealias EnvironmentKey = String
internal typealias RuleKey = String

public data class Rule(
    val key: RuleKey,
    val segments: GroupSegment,
    val percentage: Weight,

    val enabled: Boolean? = null,
    val variation: VariationValue? = null,
    val variables: VariableValues? = null,
)

public data class Environment(
    val expose: Boolean? = null,
    val rules: List<Rule>,
    val force: List<Force>? = null,
)

public typealias Environments = Map<EnvironmentKey, Environment>

public data class ParsedFeature(
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

public typealias AttributeKey = String

@Serializable
public data class Attribute(
    val key: AttributeKey,
    val type: String,
    val archived: Boolean? = null,
    val capture: Boolean? = null,
)

public sealed class AttributeValue {
    public data class StringValue(val value: String?) : AttributeValue()
    public data class IntValue(val value: Int) : AttributeValue()
    public data class DoubleValue(val value: Double) : AttributeValue()
    public data class BooleanValue(val value: Boolean) : AttributeValue()
    public data class DateValue(val value: Date) : AttributeValue()
}

@Serializable(with = ConditionSerializer::class)
public sealed class Condition {
    public data class Plain(
        val attributeKey: AttributeKey,
        val operator: Operator,
        val value: ConditionValue,
    ) : Condition()

    public data class And(val and: List<Condition>) : Condition()
    public data class Or(val or: List<Condition>) : Condition()
    public data class Not(val not: List<Condition>) : Condition()
}

public const val TAG: String = "FeaturevisorService"

@Serializable(with = ConditionValueSerializer::class)
public sealed class ConditionValue {
    public data class StringValue(val value: String?) : ConditionValue()
    public data class IntValue(val value: Int) : ConditionValue()
    public data class DoubleValue(val value: Double) : ConditionValue()
    public data class BooleanValue(val value: Boolean) : ConditionValue()
    public data class ArrayValue(val values: List<String>) : ConditionValue()
    public data class DateTimeValue(val value: Date) : ConditionValue()
}

public typealias SegmentKey = String

@Serializable
public data class Segment(
    val archived: Boolean? = null,
    val key: SegmentKey,
    val conditions: Condition,
)

public data class AndGroupSegment(
    val and: List<GroupSegment>,
)

public data class OrGroupSegment(
    val or: List<GroupSegment>,
)

public data class NotGroupSegment(
    val not: List<GroupSegment>,
)

@Serializable(with = GroupSegmentSerializer::class)
public sealed class GroupSegment {
    public data class Plain(val segment: SegmentKey) : GroupSegment()
    public data class Multiple(val segments: List<GroupSegment>) : GroupSegment()
    public data class And(val segment: AndGroupSegment) : GroupSegment()
    public data class Or(val segment: OrGroupSegment) : GroupSegment()
    public data class Not(val segment: NotGroupSegment) : GroupSegment()
}

public enum class Operator(public val value: String) {
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

public enum class EventName {
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
internal typealias Percentage = Int

internal typealias Range = List<Int>

@Serializable
public data class Allocation(
    val variation: VariationValue,
    val range: Range,
)

@Serializable
public data class Traffic(
    val key: RuleKey,
    val segments: GroupSegment,
    val percentage: Percentage,

    val enabled: Boolean? = null,
    val variation: VariationValue? = null,
    val variables: VariableValues? = null,

    val allocation: List<Allocation>,
)

@Serializable(with = BucketBySerializer::class)
public sealed class BucketBy {
    public data class Single(val bucketBy: String) : BucketBy()
    public data class And(val bucketBy: List<String>) : BucketBy()
    public data class Or(val bucketBy: List<String>) : BucketBy()
}

public data class RequiredWithVariation(
    val key: FeatureKey,
    val variation: VariationValue,
)

@Serializable(with = RequiredSerializer::class)
public sealed class Required {
    public data class FeatureKey(val required: com.featurevisor.types.FeatureKey) : Required()
    public data class WithVariation(val required: RequiredWithVariation) : Required()
}

@Serializable
public data class Feature(
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
public data class DatafileContent(
    val schemaVersion: String,
    val revision: String,
    val attributes: List<Attribute>,
    val segments: List<Segment>,
    val features: List<Feature>,
)

@Serializable
public data class OverrideFeature(
    val enabled: Boolean,
    val variation: VariationValue? = null,
    val variables: VariableValues? = null,
)

/**
 * Tests
 */

internal typealias AssertionMatrix = Map<String, List<AttributeValue>>


internal data class FeatureAssertion(
    var description: String?=null,
    var environment: EnvironmentKey="staging",
    // bucket weight: 0 to 100
    var at: WeightType = WeightType.IntType(40),
    var context: Context = mapOf("devMode" to AttributeValue.BooleanValue(false)),
    val expectedToBeEnabled: Boolean?=null,
    val expectedVariation: VariationValue?=null,
    val expectedVariables: VariableValues?=null,
    val matrix: AssertionMatrix? = null
)

internal data class TestFeature(
    val key: FeatureKey,
    val assertions: List<FeatureAssertion>,
)

internal data class SegmentAssertion(
    var description: String?=null,
    var context: Context,
    val expectedToMatch: Boolean,
    val matrix: AssertionMatrix? = null
)

internal data class TestSegment(
    val key: SegmentKey,
    val assertions: List<SegmentAssertion>,
)

internal sealed class Test {
    data class Feature(val value: TestFeature) : Test()
    data class Segment(val value: TestSegment) : Test()
}

internal sealed class WeightType{
    data class IntType(val value: Int):WeightType()

    data class DoubleType(val value: Double):WeightType()

    data class StringType(val value: String):WeightType()
}

internal data class TestResultAssertionError(
    val type: String,
    val expected: Any?=null,
    val actual: Any?=null,
    val message: String?=null,
    val details: Map<String, Any>?=null
)

internal data class TestResultAssertion(
    val description: String,
    val environment: EnvironmentKey? = null,
    var duration: Long,
    var passed: Boolean,
    val errors: List<TestResultAssertionError>?
)

internal data class TestResult(
    val type: String,
    val key: FeatureKey,
    var notFound: Boolean?=null,
    var passed: Boolean,
    var duration: Long,
    val assertions: List<TestResultAssertion>
)

internal data class ExecutionResult(
    var passed: Boolean,
    val assertionsCount: AssertionsCount
)

internal data class AssertionsCount(
    var passed: Int=0,
    var failed: Int=0
)

internal data class DataFile(
    val stagingDataFiles: DatafileContent? = null,
    val productionDataFiles: DatafileContent? = null
)
