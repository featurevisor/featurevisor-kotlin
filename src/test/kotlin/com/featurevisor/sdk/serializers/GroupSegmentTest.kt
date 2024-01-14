package com.featurevisor.sdk.serializers

import com.featurevisor.types.GroupSegment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class GroupSegmentTest {

    @Test
    fun `decode PLAIN segment`() {
        val element = """
            testSegment
        """.trimIndent()

        val groupSegment = Json.decodeFromString<GroupSegment>(element)

        groupSegment.shouldBeTypeOf<GroupSegment.Plain>()
        groupSegment.segment shouldBe "testSegment"
    }

    @Test
    fun `decode AND group segment`() {
        val element = """
            {
                "and": ["testSegment1", "testSegment2"]
            }
        """.trimIndent()

        val groupSegment = Json.decodeFromString<GroupSegment>(element)

        groupSegment.shouldBeTypeOf<GroupSegment.And>()
        groupSegment.segment.and[0] shouldBe GroupSegment.Plain("testSegment1")
        groupSegment.segment.and[1] shouldBe GroupSegment.Plain("testSegment2")
    }

    @Test
    fun `decode OR group segment`() {
        val element = """
            {
                "or": ["testSegment1", "testSegment2"]
            }
        """.trimIndent()

        val groupSegment = Json.decodeFromString<GroupSegment>(element)

        groupSegment.shouldBeTypeOf<GroupSegment.Or>()
        groupSegment.segment.or[0] shouldBe GroupSegment.Plain("testSegment1")
        groupSegment.segment.or[1] shouldBe GroupSegment.Plain("testSegment2")
    }

    @Test
    fun `decode NOT group segment`() {
        val element = """
            {
                "not": ["testSegment1", "testSegment2"]
            }
        """.trimIndent()

        val groupSegment = Json.decodeFromString<GroupSegment>(element)

        groupSegment.shouldBeTypeOf<GroupSegment.Not>()
        groupSegment.segment.not[0] shouldBe GroupSegment.Plain("testSegment1")
        groupSegment.segment.not[1] shouldBe GroupSegment.Plain("testSegment2")
    }
}
