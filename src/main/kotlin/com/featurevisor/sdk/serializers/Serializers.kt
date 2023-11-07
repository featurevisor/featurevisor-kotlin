package com.featurevisor.sdk.serializers

import com.featurevisor.sdk.BucketBy
import com.featurevisor.sdk.Condition
import com.featurevisor.sdk.ConditionValue
import com.featurevisor.sdk.GroupSegment
import com.featurevisor.sdk.Operator
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

@OptIn(InternalSerializationApi::class)
@Serializer(forClass = Condition::class)
object ConditionSerializer : KSerializer<Condition> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.Condition", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): Condition {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be decoded only by Json format")
        return when (val tree = input.decodeJsonElement()) {
            is JsonArray -> {
                Condition.And(tree.map { jsonElement ->
                    input.json.decodeFromJsonElement(
                        Condition::class.serializer(),
                        jsonElement
                    )
                })
            }

            is JsonObject -> {
                Condition.Plain(
                    attributeKey = tree["attribute"]?.jsonPrimitive?.content ?: "",
                    operator = mapOperator(tree["operator"]?.jsonPrimitive?.content ?: ""),
                    value = input.json.decodeFromJsonElement(
                        ConditionValue::class.serializer(),
                        tree["value"]!!
                    ),
                )
            }

            is JsonPrimitive -> {
                val jsonElement = input.json.parseToJsonElement(tree.content)
                input.json.decodeFromJsonElement(
                    Condition::class.serializer(),
                    jsonElement
                )
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Condition) {
        // TODO: Later if needed
    }
}

@OptIn(InternalSerializationApi::class)
@Serializer(forClass = GroupSegment::class)
object GroupSegmentSerializer : KSerializer<GroupSegment> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.GroupSegment", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): GroupSegment {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be decoded only by Json format")
        return when (val tree = input.decodeJsonElement()) {
            is JsonArray -> GroupSegment.Multiple(tree.map { jsonElement ->
                input.json.decodeFromJsonElement(
                    GroupSegment::class.serializer(),
                    jsonElement
                )
            })

            is JsonObject -> {
                // TODO:
                GroupSegment.Plain("")
            }

            is JsonPrimitive -> {
                val isString = tree.content.none {it in setOf('{', '}', ':', '[', ']')}
                if (isString) {
                    GroupSegment.Plain(tree.content)
                } else {
                    val jsonElement = Json.parseToJsonElement(tree.content)
                    input.json.decodeFromJsonElement(
                        GroupSegment::class.serializer(),
                        jsonElement,
                    )
                }
            }
        }
    }

    override fun serialize(encoder: Encoder, value: GroupSegment) {
        // TODO: Later if needed
    }
}

@OptIn(InternalSerializationApi::class)
@Serializer(forClass = BucketBy::class)
object BucketBySerializer : KSerializer<BucketBy> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.BucketBy", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): BucketBy {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be decoded only by Json format")
        return when (val tree = input.decodeJsonElement()) {
            is JsonArray -> {
                BucketBy.And(tree.map { jsonElement ->
                    jsonElement.jsonPrimitive.content
                })
            }

            is JsonObject -> {
                when {
                    tree.containsKey("or") -> BucketBy.Or(tree["or"]!!.jsonArray.map { it.jsonPrimitive.content })
                    tree.containsKey("and") -> BucketBy.And(tree["and"]!!.jsonArray.map { it.jsonPrimitive.content })
                    else -> throw Exception("Unexpected BucketBy element content")
                }
            }

            is JsonPrimitive -> {
                val isString = tree.content.none {it in setOf('{', '}', ':', '[', ']')}
                if (isString) {
                    BucketBy.Single(tree.content)
                } else {
                    val jsonElement = Json.parseToJsonElement(tree.content)
                    input.json.decodeFromJsonElement(
                        BucketBy::class.serializer(),
                        jsonElement
                    )
                }
            }
        }
    }

    override fun serialize(encoder: Encoder, value: BucketBy) {
        // TODO: Later if needed
    }
}

@OptIn(InternalSerializationApi::class)
@Serializer(forClass = ConditionValue::class)
object ConditionValueSerializer : KSerializer<ConditionValue> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.ConditionValue", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): ConditionValue {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be decoded only by Json format")
        return when (val tree = input.decodeJsonElement()) {
            is JsonPrimitive -> {
                tree.intOrNull?.let {
                    ConditionValue.IntValue(it)
                } ?: tree.booleanOrNull?.let {
                    ConditionValue.BooleanValue(it)
                } ?: tree.doubleOrNull?.let {
                    ConditionValue.DoubleValue(it)
                } ?: tree.content.let {
                    ConditionValue.StringValue(it)
                    // TODO:
//                    ConditionValue.DateTimeValue
                }
            }

            is JsonArray -> {
                ConditionValue.ArrayValue(tree.jsonArray.map { jsonElement -> jsonElement.jsonPrimitive.content })
            }

            is JsonObject -> {
                throw NotImplementedError("ConditionValue does not support JsonObject")
            }
        }
    }

    override fun serialize(encoder: Encoder, value: ConditionValue) {
        // TODO: Later if needed
    }
}

private fun mapOperator(value: String): Operator {
    return when (value.trim()) {
        "equals" -> Operator.EQUALS
        "notEquals" -> Operator.NOT_EQUALS

        // numeric
        "greaterThan" -> Operator.GREATER_THAN
        "greaterThanOrEqual" -> Operator.GREATER_THAN_OR_EQUAL
        "lessThan" -> Operator.LESS_THAN
        "lessThanOrEqual" -> Operator.LESS_THAN_OR_EQUAL

        // string
        "contains" -> Operator.CONTAINS
        "notContains" -> Operator.NOT_CONTAINS
        "startsWith" -> Operator.STARTS_WITH
        "endsWith" -> Operator.ENDS_WITH

        // semver (string)
        "semverEquals" -> Operator.SEMVER_EQUALS
        "semverNotEquals" -> Operator.SEMVER_NOT_EQUALS
        "semverGreaterThan" -> Operator.SEMVER_GREATER_THAN
        "semverGreaterThanOrEqual" -> Operator.SEMVER_GREATER_THAN_OR_EQUAL
        "semverLessThan" -> Operator.SEMVER_LESS_THAN
        "semverLessThanOrEqual" -> Operator.SEMVER_LESS_THAN_OR_EQUAL

        // date comparisons
        "before" -> Operator.BEFORE
        "after" -> Operator.AFTER

        // array of strings
        "in" -> Operator.IN_ARRAY
        "not" -> Operator.NOT_IN_ARRAY
        else -> throw Exception("Unexpected value of operator: $value")
    }
}
