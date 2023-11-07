package com.featurevisor.sdk.factory

import com.featurevisor.sdk.Attribute
import com.featurevisor.sdk.BucketBy
import com.featurevisor.sdk.Condition
import com.featurevisor.sdk.ConditionValue
import com.featurevisor.sdk.DatafileContent
import com.featurevisor.sdk.Feature
import com.featurevisor.sdk.Operator.EQUALS
import com.featurevisor.sdk.Operator.NOT_EQUALS
import com.featurevisor.sdk.Segment

object DatafileContentFactory {

    fun get() = DatafileContent(
        schemaVersion = "schemaVersion",
        revision = "revision",
        // Attributes are the building blocks of creating segments. They are the properties that you can use to target users.
        attributes = getAttributes(),
        // Segments are made up of conditions against various attributes. They are the groups of users that you can target.
        segments = getSegments(),
        // Features are the building blocks of creating traditional boolean feature flags and more advanced multivariate experiments.
        features = getFeatures(),
    )

    fun getAttributes() = listOf(
        Attribute(
            key = "browser_type",
            type = "string",
            archived = false,
            capture = true,
        ),
        Attribute(
            key = "device",
            type = "string",
            archived = false,
            capture = true,
        ),
    )

    fun getSegments() = listOf(
        Segment(
            archived = false,
            key = "netherlands",
            conditions = Condition.And(
                listOf(
                    Condition.Plain(
                        attributeKey = "browser_type",
                        operator = EQUALS,
                        value = ConditionValue.StringValue("chrome"),
                    ),
                    Condition.Plain(
                        attributeKey = "device",
                        operator = NOT_EQUALS,
                        value = ConditionValue.StringValue("tablet"),
                    )
                ),
            ),
        ),
    )

    fun getFeatures() = listOf(
        Feature(
            key = "landing_page",
            deprecated = false,
            variablesSchema = null,
            variations = null,
            bucketBy = BucketBy.Single("userId"),
            required = null,
            traffic = emptyList(),
            force = null,
            ranges = null,
        )
    )
}
