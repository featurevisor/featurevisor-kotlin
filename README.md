# featurevisor-kotlin

This repository is a work in progress to port the [Featurevisor](https://featurevisor.com) JavaScript SDK to Kotlin for supporting Android apps.

We are not ready yet. Please come back later.

## Installation

...

## Usage

...

## Development

We wish to reach feature parity with the existing JavaScript SDK: https://featurevisor.com/docs/sdks/

### Requirements

Install dependencies with Homebrew:

```
$ brew install openjdk
$ brew install gradle
```

### Running locally

```
$ gradle build
$ gradle test
```

### TODOs

- [ ] Add documentation in README for Installation and Usage sections
- [ ] Verify if `JavaLanguageVersion` set in `build.gradle.kt` is correct (set to `8` now)
- [ ] Set up CI/CD pipeline
- [ ] Publish as package
- [ ] Find a compatible MurmurHash implementation for Kotlin/JVM
- [ ] Port the SDK API as defined below

### SDK API

We are breaking down the various parts that we need to migrate to Swift in the sections below:

| Section             | Task                                             | Status |
|---------------------|--------------------------------------------------|--------|
| Files               | `@featurevisor/types` ➡️ `Types.kt`              | ✅      |
|                     | SDK's `bucket.ts` ➡️ `bucket.kt`                 |        |
|                     | SDK's `conditions.ts` ➡️ `conditions.kt`         |        |
|                     | SDK's `datafileReader.ts` ➡️ `DatafileReader.kt` |        |
|                     | SDK's `emitter.ts` ➡️ `Emitter.kt`               |        |
|                     | SDK's `feature.ts` ➡️ `Emitter.kt`               |        |
|                     | SDK's `instance.ts` ➡️ `Instance.kt`             |        |
|                     | SDK's `logger.ts` ➡️ `Logger.kt`                 |        |
|                     | SDK's `segments.ts` ➡️ `segments.kt`             |        |
|                     |                                                  |        |
| Constructor options | `bucketKeySeparator`                             |        |
|                     | `configureBucketKey`                             |        |
|                     | `configureBucketValue`                           |        |
|                     | `datafile`                                       |        |
|                     | `datafileUrl`                                    |        |
|                     | `handleDatafileFetch`                            |        |
|                     | `initialFeatures`                                |        |
|                     | `interceptContext`                               |        |
|                     | `logger`                                         |        |
|                     | `onActivation`                                   |        |
|                     | `onReady`                                        |        |
|                     | `onRefresh`                                      |        |
|                     | `onUpdate`                                       |        |
|                     | `refreshInternal`                                |        |
|                     | `stickyFeatures`                                 |        |
|                     |                                                  |        |
| Instance methods    | `constructor` missing fetch datafile content     |        |
|                     | `setDatafile` removed to workaround init issues  |        |
|                     | `setStickyFeatures`                              |        |
|                     | `getRevision`                                    |        |
|                     | `getFeature`                                     |        |
|                     | `getBucketKey`                                   |        |
|                     | `getBucketValue`                                 |        |
|                     | `isReady`                                        |        |
|                     | `refresh`                                        |        |
|                     | `startRefreshing`                                |        |
|                     | `stopRefreshing`                                 |        |
|                     | `evaluateFlag`                                   |        |
|                     | `isEnabled`                                      |        |
|                     | `evaluateVariation`                              |        |
|                     | `getVariation`                                   |        |
|                     | `activate`                                       |        |
|                     | `evaluateVariable`                               |        |
|                     | `getVariable`                                    |        |
|                     | `getVariableBoolean`                             |        |
|                     | `getVariableString`                              |        |
|                     | `getVariableInteger`                             |        |
|                     | `getVariableDouble`                              |        |
|                     | `getVariableArray`                               |        |
|                     | `getVariableObject`                              |        |
|                     | `getVariableJSON`                                |        |
|                     |                                                  |        |
| Functions           | `createInstance` missing proper error handling   |        |
|                     | `fetchDatafileContent`                           |        |
|                     | `getValueByType`                                 |        |

### Test runner

We should also have an executable as an output of this repository that can be used to run the test specs against the Kotlin SDK: https://featurevisor.com/docs/testing/

Example command:

```
$ cd path/to/featurevisor-project-with-yamls
$ featurevisor-kotlin test
```

## License

MIT © [Fahad Heylaal](https://fahad19.com)
