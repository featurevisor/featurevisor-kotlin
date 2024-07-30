package com.featurevisor.sdk

import com.featurevisor.types.*

class DatafileReader(
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

    fun getAllAttributes(): Map<AttributeKey, Attribute> {
        return attributes
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
