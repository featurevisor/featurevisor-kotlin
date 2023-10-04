package com.featurevisor.types

enum class Operator(val value: String) {
    EQUALS("equals"),
    NOT_EQUALS("notEquals"),

    // numeric
    GREATER_THAN("greaterThan"),
    GREATER_THAN_OR_EQUAL("greaterThanOrEqual"),
    LESS_THAN("lessThan"),
    LESS_THAN_OR_EQUAL("lessThanOrEqual"),

    // string
    CONTAINS("contains"),
    NOT_CONTAINS("notContains"),
    STARTS_WITH("startsWith"),
    ENDS_WITH("endsWith"),

    // semver (string)
    SEMVER_EQUALS("semverEquals"),
    SEMVER_NOT_EQUALS("semverNotEquals"),
    SEMVER_GREATER_THAN("semverGreaterThan"),
    SEMVER_GREATER_THAN_OR_EQUAL("semverGreaterThanOrEqual"),
    SEMVER_LESS_THAN("semverLessThan"),
    SEMVER_LESS_THAN_OR_EQUAL("semverLessThanOrEqual"),

    // date comparisons
    BEFORE("before"),
    AFTER("after"),

    // array of strings
    IN_ARRAY("inArray"),
    NOT_IN_ARRAY("notInArray");
}
