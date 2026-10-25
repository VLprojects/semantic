# semantic-types

Rich value objects for Kotlin and Java, one module for each kind of value: a
service takes versions without anything else. The family does not depend on
`ddd`.

## Versions

Package `pro.vlprojects.semantic.types.version` of `semantic-types-version`.
A version tells one state of a thing from another. What else it can do
depends on its kind, and each kind says it with a supertype:

| Type | What it is |
| --- | --- |
| `Version` | a version of any kind: it is only equal or not to another one, like an ETag or a hash; `Serializable` |
| `OrderedVersion<V>` | a version that is older or newer than any other version of its kind |
| `IncrementableVersion<V>` | an ordered version that goes up by one step: `next()` is newer than it |
| `VersionFormat<V>` | reads a version of its kind from text: `parse` fails on a wrong text, `parseOrNull` gives `null` |
| `SequentialVersion` | a counter: `0`, `1`, `2`; the version of a schema, of an event, of a document |

`Comparable` is not on `Version`: a hash has no order, and a vector clock
leaves two versions unordered when they changed apart. An ordered version is
compared only with its own kind, so a sequential version is never compared
with a semantic one by mistake.

```kotlin
val version = SequentialVersion.parse("3")

version.next()                        // 4
version < SequentialVersion.of(10)    // true
SequentialVersion.parseOrNull("v3")   // null
```

The format of a version is its companion: `SequentialVersion.Format` in Java.
A sequential version is written as a canonical number, with no sign and no
leading zeros, and starts at `SequentialVersion.INITIAL`, zero.

Semantic and calendar versions come next.

## Modules

One artifact so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-types-version` | any JVM code: versions of every kind | nothing |

```
types/
└── version/               semantic-types-version
```
