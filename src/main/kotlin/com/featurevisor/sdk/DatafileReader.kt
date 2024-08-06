package com.featurevisor.sdk

import com.featurevisor.types.Attribute
import com.featurevisor.types.AttributeKey
import com.featurevisor.types.DatafileContent
import com.featurevisor.types.Feature
import com.featurevisor.types.FeatureKey
import com.featurevisor.types.Segment
import com.featurevisor.types.SegmentKey

class DatafileReader constructor(
    datafileContent: DatafileContent,
) {

    private val schemaVersion: String = datafileContent.schemaVersion
    private val revision: String = datafileContent.revision
    private val attributes: Map<AttributeKey, Attribute> = datafileContent.attributes.associateBy { it.key }
    private val segments: Map<SegmentKey, Segment> = datafileContent.segments.associateBy { it.key }
    private val features: Map<FeatureKey, Feature> = datafileContent.features.associateBy { it.key }

    fun getRevision(): String {
        return revision
    }

    fun getSchemaVersion(): String {
        return schemaVersion
    }

    fun getAllAttributes(): List<Attribute> {
        return attributes.values.toList()
    }

    fun getAttribute(attributeKey: AttributeKey): Attribute? {
        return attributes[attributeKey]
    }

    fun getSegment(segmentKey: SegmentKey): Segment? {
        return segments[segmentKey]
    }

    fun getFeature(featureKey: FeatureKey): Feature? {
        return features[featureKey]
    }
}
