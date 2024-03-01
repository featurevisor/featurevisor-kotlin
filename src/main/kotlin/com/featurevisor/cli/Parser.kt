package com.featurevisor.cli

import com.featurevisor.sdk.serializers.mapOperator
import com.featurevisor.types.*
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.time.LocalDate

 internal fun parseYamlAssertions(yamlFilePath: String) =
    try {
        val yamlContent = File(yamlFilePath).readText()

        val yaml = org.yaml.snakeyaml.Yaml()
        val data = yaml.load<Map<String, Any>>(yamlContent)

        val feature = data["feature"] as? String
        val assertions = (data["assertions"] as? List<Map<String, Any>>)?.map { assertionMap ->
            Assertion(
                description = assertionMap["description"] as? String,
                environment = assertionMap["environment"] as? String,
                at = assertionMap["at"] as? Double,
                context = (assertionMap["context"] as Map<AttributeKey, Any>).mapValues { parseAttributeValue(it.value) },
                expectedToBeEnabled = assertionMap["expectedToBeEnabled"] as? Boolean,
                expectedVariables = (assertionMap["expectedVariables"] as? Map<String, Any?>)?.mapValues { parseVariableValue(it.value) },
                expectedVariation = assertionMap["expectedVariation"] as? String,
                expectedToMatch = assertionMap["expectedToMatch"] as? Boolean,
            )
        }

        Spec(feature = feature, assertion = assertions)
    } catch (e: Exception) {
        printMessageInRedColor("Exception while parsing Yaml Assertion File  --> ${e.message}")
        null
    }

private fun parseVariableValue(value: Any?): VariableValue {
    return when (value) {
        is Boolean -> VariableValue.BooleanValue(value)
        is String -> VariableValue.StringValue(value)
        is Int -> VariableValue.IntValue(value)
        is Double -> VariableValue.DoubleValue(value)
        is List<*> -> {
            val stringList = value.filterIsInstance<String>()
            VariableValue.ArrayValue(stringList)
        }
        is Map<*, *> -> {
            val mapData = value as Map<String, Any>
            val attributeMap = mapData.mapValues { parseVariableValue(it.value) }
            VariableValue.ObjectValue(attributeMap)
        }
        else -> throw IllegalArgumentException("Unsupported variable value type")
    }
}

private fun parseAttributeValue(value: Any?): AttributeValue {
    return when (value) {
        is String -> AttributeValue.StringValue(value)
        is Int -> AttributeValue.IntValue(value)
        is Double -> AttributeValue.DoubleValue(value)
        is Boolean -> AttributeValue.BooleanValue(value)
        is LocalDate -> AttributeValue.DateValue(value)
        else -> throw IllegalArgumentException("Unsupported attribute value type")
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

private fun parseConditionValue(value: Any?) =
    when (value) {
        is String -> ConditionValue.StringValue(value)
        is Int -> ConditionValue.IntValue(value)
        is Double -> ConditionValue.DoubleValue(value)
        is Boolean -> ConditionValue.BooleanValue(value)
        is List<*> -> {
            val stringList = value.filterIsInstance<String>()
            ConditionValue.ArrayValue(stringList)
        }

        else -> throw IllegalArgumentException("Unsupported condition value type")
    }
