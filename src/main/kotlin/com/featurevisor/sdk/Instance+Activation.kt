package com.featurevisor.sdk

import com.featurevisor.types.AttributeValue
import com.featurevisor.types.Context
import com.featurevisor.types.EventName.ACTIVATION
import com.featurevisor.types.FeatureKey
import com.featurevisor.types.VariationValue

fun FeaturevisorInstance.activate(featureKey: FeatureKey, context: Context = emptyMap()): VariationValue? {
   return try {
       val evaluation = evaluateVariation(featureKey, context)
       val variationValue = evaluation.variation?.value ?: evaluation.variationValue ?: return null
       val finalContext = interceptContext?.invoke(context) ?: context
       val captureContext = mutableMapOf<String, AttributeValue>()
       val attributesForCapturing = datafileReader.getAllAttributes()
           .filter { it.capture == true }

       attributesForCapturing.forEach { attribute ->
           finalContext[attribute.key]?.let {
               captureContext[attribute.key] = it
           }
       }

       emitter.emit(
           ACTIVATION,
           featureKey,
           variationValue,
           finalContext,
           captureContext,
           evaluation
       )

        variationValue
    }catch (e:Exception){
       FeaturevisorInstance.companionLogger?.error("Exception in activate() -> $e")
        null
    }
}
