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
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
@Serializer(forClass = Required::class)
object RequiredSerializer : KSerializer<Required> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.Required", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): Required {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can only be decoded using the Json format")

        return when (val element = input.decodeJsonElement()) {
            is JsonPrimitive -> Required.FeatureKey(element.content)
            is JsonObject -> {
                val key = element["key"]?.jsonPrimitive?.content.orEmpty()
                val variation = element["variation"]?.jsonPrimitive?.content.orEmpty()
                Required.WithVariation(RequiredWithVariation(key, variation))
            }

            else -> throw SerializationException("Unexpected JSON element: ${element::class.simpleName}")
        }
    }
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
object ConditionSerializer : KSerializer<Condition> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.Condition", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): Condition {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can only be decoded using the Json format")

        return when (val element = input.decodeJsonElement()) {
            is JsonArray -> {
                val conditions = element.map {
                    input.json.decodeFromJsonElement(Condition.serializer(), it)
                }
                Condition.And(conditions)
            }

            is JsonObject -> {
                FeaturevisorInstance.companionLogger?.debug(
                    "Segment deserializing: ${element["attribute"]?.jsonPrimitive?.content}, tree: $element"
                )

                when {
                    "and" in element -> Condition.And(
                        element["and"]!!.jsonArray.map {
                            input.json.decodeFromJsonElement(Condition.serializer(), it)
                        }
                    )

                    "or" in element -> Condition.Or(
                        element["or"]!!.jsonArray.map {
                            input.json.decodeFromJsonElement(Condition.serializer(), it)
                        }
                    )

                    "not" in element -> Condition.Not(
                        element["not"]!!.jsonArray.map {
                            input.json.decodeFromJsonElement(Condition.serializer(), it)
                        }
                    )

                    else -> Condition.Plain(
                        attributeKey = element["attribute"]?.jsonPrimitive?.content.orEmpty(),
                        operator = mapOperator(element["operator"]?.jsonPrimitive?.content.orEmpty()),
                        value = input.json.decodeFromJsonElement(
                            ConditionValue.serializer(),
                            element["value"]!!
                        )
                    )
                }
            }

            is JsonPrimitive -> {
                val parsedElement = input.json.parseToJsonElement(element.content)
                input.json.decodeFromJsonElement(Condition.serializer(), parsedElement)
            }

            else -> throw SerializationException("Unexpected JSON element: ${element::class.simpleName}")
        }
    }

    override fun serialize(encoder: Encoder, value: Condition) {
        // TODO: Implement if serialization is required in the future
    }
}

@OptIn(InternalSerializationApi::class)
object GroupSegmentSerializer : KSerializer<GroupSegment> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.GroupSegment", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): GroupSegment {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can only be decoded by Json format")

        return when (val jsonElement = input.decodeJsonElement()) {
            is JsonArray -> parseJsonArray(input, jsonElement)
            is JsonObject -> parseJsonObject(input, jsonElement)
            is JsonPrimitive -> parseJsonPrimitive(input, jsonElement)
            else -> throw SerializationException("Unexpected GroupSegment element type")
        }
    }

    private fun parseJsonArray(input: JsonDecoder, jsonArray: JsonArray): GroupSegment.Multiple {
        val elements = jsonArray.map {
            input.json.decodeFromJsonElement(GroupSegment::class.serializer(), it)
        }
        return GroupSegment.Multiple(elements)
    }

    private fun parseJsonObject(input: JsonDecoder, jsonObject: JsonObject): GroupSegment {
        val keys = jsonObject.keys
        return when {
            "and" in keys -> GroupSegment.And(
                AndGroupSegment(parseNestedArray(input, jsonObject["and"]!!.jsonArray))
            )

            "or" in keys -> GroupSegment.Or(
                OrGroupSegment(parseNestedArray(input, jsonObject["or"]!!.jsonArray))
            )

            "not" in keys -> GroupSegment.Not(
                NotGroupSegment(parseNestedArray(input, jsonObject["not"]!!.jsonArray))
            )

            else -> throw SerializationException("Unexpected GroupSegment object keys: $keys")
        }
    }

    private fun parseJsonPrimitive(input: JsonDecoder, jsonPrimitive: JsonPrimitive): GroupSegment {
        val content = jsonPrimitive.content
        return if (content.none { it in setOf('{', '}', ':', '[', ']') }) {
            GroupSegment.Plain(content)
        } else {
            val parsedElement = Json.parseToJsonElement(content)
            input.json.decodeFromJsonElement(GroupSegment::class.serializer(), parsedElement)
        }
    }

    private fun parseNestedArray(input: JsonDecoder, jsonArray: JsonArray): List<GroupSegment> {
        return jsonArray.map {
            input.json.decodeFromJsonElement(GroupSegment::class.serializer(), it)
        }
    }

    override fun serialize(encoder: Encoder, value: GroupSegment) {
        // TODO: Implement serialization logic if needed
    }
}

@OptIn(InternalSerializationApi::class)
object BucketBySerializer : KSerializer<BucketBy> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.BucketBy", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): BucketBy {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can only be decoded by Json format")

        return when (val jsonElement = input.decodeJsonElement()) {
            is JsonArray -> parseJsonArray(jsonElement)
            is JsonObject -> parseJsonObject(jsonElement)
            is JsonPrimitive -> parseJsonPrimitive(input, jsonElement)
            else -> throw SerializationException("Unexpected BucketBy element type")
        }
    }

    private fun parseJsonArray(jsonArray: JsonArray): BucketBy.And {
        val elements = jsonArray.map { it.jsonPrimitive.content }
        return BucketBy.And(elements)
    }

    private fun parseJsonObject(jsonObject: JsonObject): BucketBy {
        return when {
            "or" in jsonObject -> BucketBy.Or(
                parseJsonArrayContent(jsonObject["or"]!!.jsonArray)
            )

            "and" in jsonObject -> BucketBy.And(
                parseJsonArrayContent(jsonObject["and"]!!.jsonArray)
            )

            else -> throw SerializationException("Unexpected BucketBy object keys: ${jsonObject.keys}")
        }
    }

    private fun parseJsonPrimitive(input: JsonDecoder, jsonPrimitive: JsonPrimitive): BucketBy {
        val content = jsonPrimitive.content
        return if (content.none { it in setOf('{', '}', ':', '[', ']') }) {
            BucketBy.Single(content)
        } else {
            val parsedElement = Json.parseToJsonElement(content)
            input.json.decodeFromJsonElement(BucketBy::class.serializer(), parsedElement)
        }
    }

    private fun parseJsonArrayContent(jsonArray: JsonArray): List<String> {
        return jsonArray.map { it.jsonPrimitive.content }
    }

    override fun serialize(encoder: Encoder, value: BucketBy) {
        // TODO: Implement serialization logic if needed
    }
}

@OptIn(InternalSerializationApi::class)
object ConditionValueSerializer : KSerializer<ConditionValue> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.ConditionValue", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): ConditionValue {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can only be decoded by Json format")

        return when (val jsonElement = input.decodeJsonElement()) {
            is JsonPrimitive -> parseJsonPrimitive(jsonElement)
            is JsonArray -> parseJsonArray(jsonElement)
            is JsonObject -> throw NotImplementedError("ConditionValue does not support JsonObject")
            else -> throw SerializationException("Unexpected ConditionValue element type")
        }
    }

    private fun parseJsonPrimitive(jsonPrimitive: JsonPrimitive): ConditionValue {
        return jsonPrimitive.intOrNull?.let { ConditionValue.IntValue(it) }
            ?: jsonPrimitive.booleanOrNull?.let { ConditionValue.BooleanValue(it) }
            ?: jsonPrimitive.doubleOrNull?.let { ConditionValue.DoubleValue(it) }
            ?: parseStringValue(jsonPrimitive.content)
    }

    private fun parseStringValue(content: String): ConditionValue {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val date = dateFormat.parse(content)
            ConditionValue.DateTimeValue(date)
        } catch (e: Exception) {
            ConditionValue.StringValue(content)
        }
    }

    private fun parseJsonArray(jsonArray: JsonArray): ConditionValue.ArrayValue {
        val elements = jsonArray.map { it.jsonPrimitive.content }
        return ConditionValue.ArrayValue(elements)
    }

    override fun serialize(encoder: Encoder, value: ConditionValue) {
        // TODO: Implement serialization logic if needed
    }
}

@OptIn(InternalSerializationApi::class)
object VariableValueSerializer : KSerializer<VariableValue> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("package.VariableValue", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): VariableValue {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can only be decoded by Json format")

        return when (val jsonElement = input.decodeJsonElement()) {
            is JsonPrimitive -> parseJsonPrimitive(jsonElement)
            is JsonArray -> parseJsonArray(jsonElement)
            is JsonObject -> parseJsonObject(jsonElement)
            else -> throw SerializationException("Unexpected VariableValue element type")
        }
    }

    private fun parseJsonPrimitive(jsonPrimitive: JsonPrimitive): VariableValue {
        return when {
            jsonPrimitive.isString -> {
                if (isValidJson(jsonPrimitive.content)) {
                    VariableValue.JsonValue(jsonPrimitive.content)
                } else {
                    VariableValue.StringValue(jsonPrimitive.content)
                }
            }

            jsonPrimitive.intOrNull != null -> VariableValue.IntValue(jsonPrimitive.int)
            jsonPrimitive.booleanOrNull != null -> VariableValue.BooleanValue(jsonPrimitive.boolean)
            jsonPrimitive.doubleOrNull != null -> VariableValue.DoubleValue(jsonPrimitive.double)
            else -> VariableValue.StringValue(jsonPrimitive.content)
        }
    }

    private fun parseJsonArray(jsonArray: JsonArray): VariableValue.ArrayValue {
        val elements = jsonArray.map { it.jsonPrimitive.content }
        return VariableValue.ArrayValue(elements)
    }

    private fun parseJsonObject(jsonObject: JsonObject): VariableValue.JsonValue {
        FeaturevisorInstance.companionLogger?.debug("VariableValueSerializer, JsonObject: $jsonObject")
        return VariableValue.JsonValue(jsonObject.toString())
    }

    override fun serialize(encoder: Encoder, value: VariableValue) {
        // TODO: Implement serialization logic if needed
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
