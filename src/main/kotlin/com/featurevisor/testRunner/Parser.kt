package com.featurevisor.testRunner

import com.featurevisor.sdk.serializers.isValidJson
import com.featurevisor.sdk.serializers.mapOperator
import com.featurevisor.types.*
import com.google.gson.Gson
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

internal fun parseTestFeatureAssertions(yamlFilePath: String) =
    try {
        val yamlContent = File(yamlFilePath).readText()

        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(yamlContent)

        val feature = data["feature"] as? String
        val segment = data["segment"] as? String

        if (!segment.isNullOrEmpty()) {
            val segmentAssertion = (data["assertions"] as? List<Map<String, Any>>)!!.map { assertionMap ->
                SegmentAssertion(
                    description = assertionMap["description"] as? String,
                    context = (assertionMap["context"] as Map<AttributeKey, Any?>).mapValues { parseAttributeValue(it.value) },
                    expectedToMatch = assertionMap["expectedToMatch"] as Boolean,
                    matrix = (assertionMap["matrix"] as? Map<String, List<Any>>)?.mapValues {
                        it.value.map { item ->
                            mapMatrixValues(item)
                        }
                    }
                )
            }
            val testSegment = TestSegment(key = segment, assertions = segmentAssertion)
            Test.Segment(testSegment)
        } else if (!feature.isNullOrEmpty()) {
            val featureAssertion = (data["assertions"] as? List<Map<String, Any>>)!!.map { assertionMap ->
                FeatureAssertion(
                    description = assertionMap["description"] as? String,
                    environment = assertionMap["environment"] as String,
                    at = parseWeightValue((assertionMap["at"] as Any)),
                    context = (assertionMap["context"] as Map<AttributeKey, Any?>).mapValues { parseAttributeValue(it.value) },
                    expectedToBeEnabled = assertionMap["expectedToBeEnabled"] as Boolean,
                    expectedVariables = (assertionMap["expectedVariables"] as? Map<String, Any?>)?.mapValues {
                        parseVariableValue(
                            it.value
                        )
                    },
                    expectedVariation = assertionMap["expectedVariation"] as? String,
                    matrix = (assertionMap["matrix"] as? Map<String, List<Any>>)?.mapValues {
                        it.value.map { item ->
                            mapMatrixValues(item)
                        }
                    }
                )
            }

            val testFeature = TestFeature(key = feature, assertions = featureAssertion)
            Test.Feature(testFeature)
        } else {
            null
        }
    } catch (e: Exception) {
        printMessageInRedColor("Exception while parsing Yaml Assertion File -- $yamlFilePath --> ${e.message}")
        null
    }

private fun mapMatrixValues(value: Any) =
    when(value){
        is Boolean -> {
            if (value){
                AttributeValue.StringValue("yes")
            }else{
                AttributeValue.StringValue("no")
            }
        }
        is Int -> {
            AttributeValue.IntValue(value)
        }
        is Double -> {
            AttributeValue.DoubleValue(value)
        }
        is String -> {
            AttributeValue.StringValue(value)
        }
        is Date -> {
            AttributeValue.DateValue(value)
        }

        else -> { AttributeValue.StringValue("")}
    }

private fun parseWeightValue(value: Any): WeightType {
    return when (value) {
        is Int -> WeightType.IntType(value)
        is Double -> WeightType.DoubleType(value)
        else -> WeightType.StringType(value.toString())
    }
}

private fun parseVariableValue(value: Any?): VariableValue {

    return when (value) {
        is Boolean -> VariableValue.BooleanValue(value)
        is String -> {
            if (isValidJson(value)) {
                VariableValue.JsonValue(value)
            } else {
                VariableValue.StringValue(value)
            }
        }

        is Int -> VariableValue.IntValue(value)
        is Double -> VariableValue.DoubleValue(value)
        is List<*> -> {
            val stringList = value.filterIsInstance<String>()
            VariableValue.ArrayValue(stringList)
        }

        is Map<*, *> -> {
            val json = Gson().toJson(value)
            VariableValue.JsonValue(json)
        }

        else -> throw IllegalArgumentException("Unsupported variable value type")
    }
}

private fun parseAttributeValue(value: Any?): AttributeValue {
    if (value == null) {
        return AttributeValue.StringValue(null)
    }

    return when (value) {
        is Int -> AttributeValue.IntValue(value)
        is Double -> AttributeValue.DoubleValue(value)
        is Boolean -> AttributeValue.BooleanValue(value)
        is String -> {
            if (value.equals("", true)) {
                AttributeValue.StringValue("")
            } else {
                value.toIntOrNull()?.let {
                    AttributeValue.IntValue(it)
                } ?: value.toDoubleOrNull()?.let {
                    AttributeValue.DoubleValue(it)
                } ?: AttributeValue.StringValue(value)
            }
        }

        is Date -> {
            AttributeValue.DateValue(value)
        }

        is List<*> -> {
            AttributeValue.StringValue(value.toString())
        }

        is Map<*, *> -> {
            val json = Gson().toJson(value)
            AttributeValue.StringValue(json)
        }

        else -> {
            throw IllegalArgumentException("Unsupported attribute value type $value")
        }
    }
}

internal fun parseYamlSegment(segmentFilePath: String) =
    try {
        val yamlContent = File(segmentFilePath).readText()

        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(yamlContent)

        val archived = data["archived"] as? Boolean
        val description = data["description"] as? String

        val conditionsData = data["conditions"]

        Segment(
            archived = archived,
            key = "",
            conditions = parseCondition(conditionsData)
        )

    } catch (e: Exception) {
        printMessageInRedColor("Exception while parsing Yaml segment Assertion File  --> ${e.message}")
        null
    }


private fun parseCondition(conditionData: Any?): Condition {
    return when (conditionData) {
        is Map<*, *> -> {
            val mapData = conditionData as Map<*, *>
            val operator = mapData.keys.firstOrNull()
            when (operator) {
                "and" -> {
                    val andConditions = (mapData[operator] as List<*>).map { parseCondition(it) }
                    Condition.And(andConditions)
                }

                "or" -> {
                    val orConditions = (mapData[operator] as List<*>).map { parseCondition(it) }
                    Condition.Or(orConditions)
                }

                "not" -> {
                    val notConditions = (mapData[operator] as List<*>).map { parseCondition(it) }
                    Condition.Not(notConditions)
                }

                else -> {
                    val attributeKey = mapData["attribute"] as AttributeKey
                    val operatorValue = mapOperator(mapData["operator"] as String)
                    val value = parseConditionValue(mapData["value"])
                    Condition.Plain(attributeKey, operatorValue, value)
                }
            }
        }

        is List<*> -> {
            val conditionsList = conditionData as List<*>
            val conditions = conditionsList.map { parseCondition(it) }
            Condition.And(conditions)
        }

        else -> throw IllegalArgumentException("Invalid condition format")
    }


}

private fun parseConditionValue(value: Any?): ConditionValue {
    if (value == null) {
        return ConditionValue.StringValue(null)
    }

    return when (value) {
        is String -> {
            value.toIntOrNull()?.let {
                ConditionValue.IntValue(value.toInt())
            } ?: value.toDoubleOrNull()?.let {
                ConditionValue.DoubleValue(value.toDouble())
            } ?: ConditionValue.StringValue(value)
        }
        is Int -> ConditionValue.IntValue(value)
        is Double -> ConditionValue.DoubleValue(value)
        is Boolean -> ConditionValue.BooleanValue(value)
        is List<*> -> {
            val stringList = value.filterIsInstance<String>()
            ConditionValue.ArrayValue(stringList)
        }

        else -> throw IllegalArgumentException("Unsupported condition value type")
    }
}

fun parseConfiguration(projectRootPath: String) =
    json.decodeFromString(Configuration.serializer(),getConfigurationJson(projectRootPath)!!)


