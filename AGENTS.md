# Overview <!-- omit in toc -->

This repository is a work-in-progress port of the [Featurevisor](https://featurevisor.com) JavaScript SDK to Kotlin, primarily to support Android apps. The goal is to reach feature parity with the JS SDK: https://featurevisor.com/docs/sdks/

- [Links](#links)
- [Stack](#stack)
- [Structure](#structure)
- [Building](#building)
- [Testing](#testing)
- [Porting work](#porting-work)
- [Documentation](#documentation)

## Links

- Website: https://featurevisor.com
- Original JS/TS monorepo: https://github.com/featurevisor/featurevisor
- Full documentation as a single llms.txt file: https://featurevisor.com/llms.txt

## Stack

- Kotlin/JVM, built with Gradle (Kotlin DSL)
- `kotlinx.serialization` for JSON, `snakeyaml`/`gson` for YAML/JSON parsing where needed
- `okhttp` for HTTP, `kotlinx-coroutines-core` for async
- `net.swiftzer.semver` for semver comparisons
- `com.goncalossilva:murmurhash` for MurmurHash bucketing (compatible with the JS SDK's hashing)
- Tests use JUnit 5, `kotlin-test-junit5`, `mockk`, and `kotest-assertions-core`
- Published to Maven Central / JitPack as `com.github.featurevisor:featurevisor-kotlin`

## Structure

- `src/main/kotlin/com/featurevisor/sdk/`: the SDK itself — `Instance.kt` plus `Instance+*.kt` files splitting instance behavior (Feature, Evaluation, Activation, Fetch, Refresh, Segments, Status, Variable, Variation), `Bucket.kt`, `Conditions.kt`, `DatafileReader.kt`, `Emitter.kt`, `Logger.kt`, `InstanceOptions.kt`
- `src/main/kotlin/com/featurevisor/sdk/serializers/`: custom kotlinx.serialization serializers for datafile types
- `src/main/kotlin/com/featurevisor/types/Types.kt`: Kotlin equivalent of `@featurevisor/types`
- `src/main/kotlin/com/featurevisor/testRunner/`: an executable test runner that runs Featurevisor test specs (YAML) against this SDK, mirroring https://featurevisor.com/docs/testing/
- `src/test/kotlin/...`: unit tests mirroring the `src/main` structure

When porting a file from the JS SDK, keep the mapping close to 1:1 (e.g. `bucket.ts` → `Bucket.kt`, `conditions.ts` → `Conditions.kt`) so behavior and structure stay easy to diff against the source of truth. Check the JS SDK implementation (`packages/sdk` in the `featurevisor/featurevisor` repo, a sibling directory if checked out locally) when porting or fixing behavior, rather than guessing at semantics.

## Building

```
$ gradle build
```

Requires `openjdk` and `gradle` (install via Homebrew). Java toolchain is pinned to 11 in `build.gradle.kts`.

## Testing

```
$ gradle test
```

To run a specific test class or method, use Gradle's `--tests` filter, e.g. `gradle test --tests "com.featurevisor.sdk.BucketTest"`.

There is also a `run-test` Gradle task wired to `com.featurevisor.cli.TestExecuter`, intended to run Featurevisor YAML test specs against this SDK (the `featurevisor-kotlin test` executable mentioned in the README).

## Porting work

The README tracks an SDK API parity checklist (constructor options, instance methods, functions) against the JS SDK. When implementing a new piece of the API, check that table and update it. Prefer matching the JS SDK's method names and semantics as closely as Kotlin idioms allow.

## Documentation

Documentation for Featurevisor itself is maintained separately in the Featurevisor website repo and published at https://featurevisor.com. This repository's README is the primary source of truth for this SDK's own installation, usage, and progress.
