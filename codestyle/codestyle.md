# Code style

Before writing your first line of code, please import `codestyles.xml` into your IDE settings from `docs/codestyles/codestyles.xml`

---

## Add trailing comma when possible and makes sense

**Note:** That makes Git history easier to traverse, adding comma to line before is not overriding this line Git history

```kotlin
val someMap = mapOf(
    "a" to "b",
    "c" to "d",
)

class GreatClass(
    val fieldA: String,
    val fieldB: String,
)
```

---

## Don't explicitly define a type when it's not required

```kotlin
// 🔴 don't
val someVariable: String = "asd"

// 🟢 do
val someVariable = "asd"
```

**Note:** If you are still missing explicit type definition, consider enabling `Type Hints` in Android Studio, IDE will add them automatically.

---

## Empty statements or lambdas

We want to make sure that empty or blank statements are not left there by accident, that's why you need to use a `doNothing()` function for marking it as intentionally empty.

```kotlin
when (x) {
    VALUE_1 -> println("1")
    VALUE_2 -> println("2")
    else -> doNothing()
}
```

```kotlin
executeMethodWithCallbacks(
    success = { view.showSomething() },
    error = { doNothing() }
)
```

---

## Functions and its parameters

### When function only returns something, remove it's body

```kotlin
// 🔴 don't
fun returnNumber(): Int {
    return 2137
}

// 🟢 do
fun returnNumber() = 2137
```

### When function only returns something and it has a long call inside, break it with new lines

```kotlin
// 🔴 don't
fun returnSomething() = thisIsVeryImportantVariableNameBecauseItsNameIsLong.andThisIsVeryImportantMethodName(thisLooksLikeAnImportantParameter)

// 🟢 do
fun returnSomething() =
    thisIsVeryImportantVariableNameBecauseItsNameIsLong.andThisIsVeryImportantMethodName(thisLooksLikeAnImportantParameter)

// 🟢 do
fun returnSomething() =
    thisIsVeryImportantVariableNameBecauseItsNameIsLong.andThisIsVeryImportantMethodName(
        thisLooksLikeAnImportantParameter,
        ohNoThisMethodHasAnotherParameter,
    )
```

### Underscore unused parameters

```kotlin
// 🔴 don't
val function: (Int, Long, Double) -> Unit = { someInt, someLong, someDouble -> println("") }

// 🟢 do
val function: (Int, Long, Double) -> Unit = { _, _, _ -> println("") }
```

### When function definition is starting to be unreadable, put parameters in separate lines

```kotlin
// 🔴 don't
fun someFunction(parameterA: ParameterOfTypeA, parameterB: ParameterOfTypeB, parameterC: ParameterOfTypeC, parameterD: ParameterOfTypeD)

// 🟢 do
fun someFunction(
    parameterA: ParameterOfTypeA,
    parameterB: ParameterOfTypeB,
    parameterC: ParameterOfTypeC,
    parameterD: ParameterOfTypeD,
)
```

### When function contains a large mix of string and non standard classes, consider adding explicit param names

```kotlin
// 🔴 don't
someFunction(
    "a",
    CustomObject(),
    "b",
    "c"
)

// 🟢 do
someFunction(
    parameterA = "a",
    customObject = CustomObject(),
    parameterB = "b",
    parameterC = "c",
)
```

### When there is need to use line separator, use system provided one

```kotlin
// 🔴 don't
listOf("hello", "world").joinToString(separator = "\n")

// 🟢 do
listOf("hello", "world").joinToString(separator = System.lineSeparator())
```

---

## Enums

### Short enums

```kotlin
enum class ShortEnum {
    VALUE_1,
    VALUE_2,
    VALUE_3,
}
```

### Enums with value

```kotlin
enum class EnumWithValue(val value: String) {
    VALUE_1("value1"),
    VALUE_2("value2"),
    VALUE_3("value3"),
}
```

### Enums with value and method

**Note:** Semicolon in new line, between enum entries and body

```kotlin
enum class EnumWithValueAndMethod(val value: String) {
    VALUE_1("value1"),
    VALUE_2("value2"),
    VALUE_3("value3"),
    ;

    companion object {
        fun fromString(string: String) = values().firstOrNull { it.value == string }
    }
}
```

---

## Annotations

### For parameters keep annotations in the same line

```kotlin
data class SomeClass(
    @SerializedName("field1") val field1: String,
    @SerializedName("field2") val field2: String?,
)
```

### For methods, fields and classes keep them in new line

```kotlin
@AnnotationForClass
class SomeClass constructor(
    @FieldAnnotation private val field1
) {
    
    @FieldAnnotation
    val field2: String
    
    @AnnotationForMethod
    fun annotatedMethod() { 
        doNothing()
    }
}
```

### In other cases refer to Official Kotlin Style Guide

[Google Kotlin Style Guide](https://android.github.io/kotlin-guides/style.html)

### Don't use Hungarian notation, if it's still on your mind for some reason

[Just Say mNo to Hungarian Notation (Jake Wharton)](http://jakewharton.com/just-say-no-to-hungarian-notation/)

### RxJava/Stream chains

Those can get pretty ugly sometimes.

If possible, avoid deep nesting in RxJava/Stream chains. Instead, try to refactor operator arguments into separate methods. A good rule of thumb for clean and readable chains: **one line = one operator**.