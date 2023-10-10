package com.featurevisor.types

typealias SegmentKey = String

data class Segment(
    val archived: Boolean?,
    val key: SegmentKey,
    val conditions: Condition,
)

typealias PlainGroupSegment = SegmentKey

data class AndGroupSegment(
    val and: List<GroupSegment>,
)

data class OrGroupSegment(
    val or: List<GroupSegment>,
)

data class NotGroupSegment(
    val not: List<GroupSegment>,
)

sealed class GroupSegment {
    data class Plain(val segment: PlainGroupSegment) : GroupSegment()
    data class Multiple(val segments: List<GroupSegment>) : GroupSegment()

    data class And(val segment: AndGroupSegment) : GroupSegment()
    data class Or(val segment: OrGroupSegment) : GroupSegment()
    data class Not(val segment: NotGroupSegment) : GroupSegment()
}
