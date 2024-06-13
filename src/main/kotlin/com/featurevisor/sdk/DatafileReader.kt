package com.featurevisor.sdk

import com.featurevisor.types.*

class DatafileReader(
    datafileJson: DatafileContent,
) {

    private val schemaVersion: String = datafileJson.schemaVersion
    private val revision: String = datafileJson.revision
    private val attributes: List<Attribute> = datafileJson.attributes
    private val segments: List<Segment> = datafileJson.segments
    private val features: List<Feature> = datafileJson.features

    fun getRevision(): String {
        return revision
    }

    fun getSchemaVersion(): String {
        return schemaVersion
    }

    fun getAllAttributes(): List<Attribute> {
        return attributes
    }

    fun getAttribute(attributeKey: AttributeKey): Attribute? {
        return attributes.find { attribute -> attribute.key == attributeKey }
    }

    fun getSegment(segmentKey: SegmentKey): Segment? {
        return segments.find { segment -> segment.key == segmentKey }
    }

    fun getFeature(featureKey: FeatureKey): Feature? {
        return features.find { feature -> feature.key == featureKey }
    }
}
