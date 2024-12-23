package com.featurevisor.sdk

import com.featurevisor.sdk.serializers.*
import com.featurevisor.types.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.modules.SerializersModule

object JsonConfigFeatureVisor {
    val json: Json by lazy {
        Json {
            serializersModule = SerializersModule {
                contextual(BucketBy::class, BucketBySerializer)
                contextual(Condition::class, ConditionSerializer)
                contextual(ConditionValue::class, ConditionValueSerializer)
                contextual(GroupSegment::class, GroupSegmentSerializer)
                contextual(Required::class, RequiredSerializer)
                contextual(VariableValue::class, VariableValueSerializer)
            }
            ignoreUnknownKeys = true
            isLenient = true
            allowStructuredMapKeys = true
        }
    }
}

fun Segment.getCondition(): Condition {
    return synchronized(this) {
        if (conditions == null) {
            conditions = JsonConfigFeatureVisor.json.decodeFromString(ConditionSerializer, conditionStrings)
        }
        conditions!!
    }
}

fun Feature.getVariablesSchema(): List<VariableSchema> {
    return synchronized(this) {
        if (variablesSchema == null) {
            variablesSchema = variablesSchemaString?.let {
                JsonConfigFeatureVisor.json.decodeFromJsonElement<List<VariableSchema>>(it)
            }
        }
        variablesSchema.orEmpty()
    }
}

fun Feature.getVariations(): List<Variation> {
    return synchronized(this) {
        if (variations == null) {
            variations = variationStrings?.let {
                JsonConfigFeatureVisor.json.decodeFromJsonElement<List<Variation>>(it)
            }
        }
        variations.orEmpty()
    }
}

fun Feature.getBucketBy(): BucketBy {
    return synchronized(this) {
        if (bucketBy == null) {
            bucketBy = JsonConfigFeatureVisor.json.decodeFromJsonElement(BucketBy.serializer(), bucketByString)
        }
        bucketBy!!
    }
}

fun Feature.getTraffic(): List<Traffic> {
    return synchronized(this) {
        if (traffic == null) {
            traffic = JsonConfigFeatureVisor.json.decodeFromJsonElement<List<Traffic>>(trafficString)
        }
        traffic.orEmpty()
    }
}

fun Feature.getForce(): List<Force> {
    return synchronized(this) {
        if (force == null) {
            force = forceString?.let {
                JsonConfigFeatureVisor.json.decodeFromJsonElement<List<Force>>(it)
            }
        }
        force.orEmpty()
    }
}

fun Feature.getRequired() = required

fun DatafileContent.getAttributes() = attributes

fun DatafileContent.getFeature() = features

fun DatafileContent.getSegment() = segments
