# semantic-math

The structures that values share, whatever they mean: how they relate to each
other on a set, and later how they combine. The family depends on nothing, and
a rich value of `types` takes from it only what it is.

| Package | What is there |
| --- | --- |
| `relation` | relations on a set: orders, the successor of a value |

## Relations

An order of values is total when any two of them compare, and Kotlin says it
with `Comparable`: the family adds no interface of its own for it. The package
holds what `Comparable` does not tell.

| Type | What it is |
| --- | --- |
| `Incrementable<T>` | a value with a successor: `next()` is the value right after it, with none between them |

A value is incrementable when its order is discrete: a counter, a day, a
sequential version. A decimal number or an instant has no successor and stays
only `Comparable`.

```kotlin
class Step(val value: Int) : Comparable<Step>, Incrementable<Step> {

    override fun next() = Step(value + 1)

    override fun compareTo(other: Step) = value.compareTo(other.value)
}

fun <T : Incrementable<T>> T.after(steps: Int) = generateSequence(this) { it.next() }.elementAt(steps)
```

## Modules

One artifact so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-math` | any JVM code: relations on a set | nothing |

```
math/                      semantic-math
```
