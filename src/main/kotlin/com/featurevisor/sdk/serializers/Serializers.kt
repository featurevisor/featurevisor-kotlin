package com.featurevisor.sdk.serializers

import com.featurevisor.sdk.FeaturevisorInstance
import com.featurevisor.types.*
import com.featurevisor.types.Required
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
@Serializer(forClass = Required::class)
object RequiredSerializer: KSerializer<Required>{
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.Required", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): Required {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be decoded only by Json format")
        return when (val tree = input.decodeJsonElement()) {
            is JsonPrimitive ->{
                Required.FeatureKey(tree.content)
            }
            is JsonArray -> {
                // Never lies in JsonArray block
                Required.FeatureKey(tree.toString())
            }
            is JsonObject ->{
                val requiredWithVariation = RequiredWithVariation(tree["key"]?.jsonPrimitive?.content.orEmpty(),tree["variation"]?.jsonPrimitive?.content.orEmpty())
                Required.WithVariation(requiredWithVariation)
            }
        }
    }
}

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
                FeaturevisorInstance.companionLogger?.debug("Segment deserializing: ${tree["attribute"]?.jsonPrimitive?.content}, tree: $tree")
                when {
                    tree.containsKey("and") -> Condition.And(
                        tree["and"]!!.jsonArray.map {
                            input.json.decodeFromJsonElement(
                                Condition::class.serializer(),
                                it
                            )
                        }
                    )

                    tree.containsKey("or") -> Condition.Or(
                        tree["or"]!!.jsonArray.map {
                            input.json.decodeFromJsonElement(
                                Condition::class.serializer(),
                                it
                            )
                        }
                    )

                    tree.containsKey("not") -> Condition.Not(
                        tree["not"]!!.jsonArray.map {
                            input.json.decodeFromJsonElement(
                                Condition::class.serializer(),
                                it
                            )
                        }
                    )

                    else -> Condition.Plain(
                        attributeKey = tree["attribute"]?.jsonPrimitive?.content ?: "",
                        operator = mapOperator(tree["operator"]?.jsonPrimitive?.content ?: ""),
                        value = input.json.decodeFromJsonElement(
                            ConditionValue::class.serializer(),
                            tree["value"]!!
                        ),
                    )
                }
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
                when {
                    tree.containsKey("and") -> GroupSegment.And(
                        AndGroupSegment(
                            tree["and"]!!.jsonArray.map {
                                input.json.decodeFromJsonElement(
                                    GroupSegment::class.serializer(),
                                    it
                                )
                            }
                        )
                    )

                    tree.containsKey("or") -> GroupSegment.Or(
                        OrGroupSegment(
                            tree["or"]!!.jsonArray.map {
                                input.json.decodeFromJsonElement(
                                    GroupSegment::class.serializer(),
                                    it
                                )
                            }
                        )
                    )

                    tree.containsKey("not") -> GroupSegment.Not(
                        NotGroupSegment(
                            tree["not"]!!.jsonArray.map {
                                input.json.decodeFromJsonElement(
                                    GroupSegment::class.serializer(),
                                    it
                                )
                            }
                        )
                    )

                    else -> throw Exception("Unexpected GroupSegment element content")
                }
            }

            is JsonPrimitive -> {
                val isString = tree.content.none { it in setOf('{', '}', ':', '[', ']') }
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
                val isString = tree.content.none { it in setOf('{', '}', ':', '[', ']') }
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
                    try {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        val date = dateFormat.parse(it)
                        ConditionValue.DateTimeValue(date)
                    }catch (e:Exception){
                        ConditionValue.StringValue(it)
                    }
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

@OptIn(InternalSerializationApi::class)
@Serializer(forClass = VariableValue::class)
object VariableValueSerializer : KSerializer<VariableValue> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.VariableValue", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): VariableValue {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be decoded only by Json format")
        return when (val tree = input.decodeJsonElement()) {
            is JsonPrimitive -> {
                if (tree.isString) {
                    if (isValidJson(tree.content)) {
                        VariableValue.JsonValue(tree.content)
                    } else {
                        VariableValue.StringValue(tree.content)
                    }
                } else {
                    tree.intOrNull?.let {
                        VariableValue.IntValue(it)
                    } ?: tree.booleanOrNull?.let {
                        VariableValue.BooleanValue(it)
                    } ?: tree.doubleOrNull?.let {
                        VariableValue.DoubleValue(it)
                    } ?: tree.content.let {
                        VariableValue.StringValue(it)
                    }
                }
            }

            is JsonArray -> {
                VariableValue.ArrayValue(tree.jsonArray.map { jsonElement -> jsonElement.jsonPrimitive.content })
            }

            is JsonObject -> {
                FeaturevisorInstance.companionLogger?.debug("VariableValueSerializer, JsonObject, tree.jsonObject: ${tree.jsonObject}, tree: $tree")
                VariableValue.JsonValue(tree.jsonObject.toString())
            }
        }
    }

    override fun serialize(encoder: Encoder, value: VariableValue) {
        // TODO: Later if needed
    }
}

fun isValidJson(jsonString: String): Boolean {
    return try {
        // Attempt to parse the string
        Json.decodeFromString<Map<String, JsonElement>>(jsonString)
        true
    } catch (e: Exception) {
        false
    }
}

internal fun mapOperator(value: String): Operator {
    return when (value.trim()) {
        "equals" -> Operator.EQUALS
        "notEquals" -> Operator.NOT_EQUALS

        // numeric
        "greaterThan" -> Operator.GREATER_THAN
        "greaterThanOrEquals" -> Operator.GREATER_THAN_OR_EQUALS
        "lessThan" -> Operator.LESS_THAN
        "lessThanOrEquals" -> Operator.LESS_THAN_OR_EQUALS

        // string
        "contains" -> Operator.CONTAINS
        "notContains" -> Operator.NOT_CONTAINS
        "startsWith" -> Operator.STARTS_WITH
        "endsWith" -> Operator.ENDS_WITH

        // semver (string)
        "semverEquals" -> Operator.SEMVER_EQUALS
        "semverNotEquals" -> Operator.SEMVER_NOT_EQUALS
        "semverGreaterThan" -> Operator.SEMVER_GREATER_THAN
        "semverGreaterThanOrEquals" -> Operator.SEMVER_GREATER_THAN_OR_EQUALS
        "semverLessThan" -> Operator.SEMVER_LESS_THAN
        "semverLessThanOrEquals" -> Operator.SEMVER_LESS_THAN_OR_EQUALS

        // date comparisons
        "before" -> Operator.BEFORE
        "after" -> Operator.AFTER

        // array of strings
        "in" -> Operator.IN_ARRAY
        "notIn" -> Operator.NOT_IN_ARRAY
        else -> throw Exception("Unexpected value of operator: $value")
    }
}
