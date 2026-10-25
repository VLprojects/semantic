# semantic-helper

Extensions of the types of Kotlin and the JDK that the other families share.
Plain Kotlin and the JDK, no dependencies.

Each group of helpers is a package under `pro.vlprojects.semantic.helper`, a
file per type it extends: `StringHelper.kt`, `InstantHelper.kt`.

| Package | Extends | What is there |
| --- | --- | --- |
| `text` | `String`, `CharSequence` | the text itself: symbols as a reader sees them, `SymbolSequence` to walk them |
| `time` | `Instant`, `LocalDate`, `Duration` | planned |
| `number` | `BigDecimal`, `Int`, `Long` | planned |
| `lang` | `Any`, `KClass` | planned |

A helper extends a type that already exists and knows nothing of the domain:
what speaks of aggregates or value objects belongs to its family. How a value
is written down, words, cases and abbreviation, is the
[`format`](../format/README.md) family.

## Symbols

A symbol is what a reader sees as one character: a letter with its accent, a
flag, an emoji of several characters. Lengths in the helpers count symbols, and
a cut never splits one.

![Symbols over the chars that store them](../docs/assets/helper-symbols-light.svg#gh-light-mode-only)
![Symbols over the chars that store them](../docs/assets/helper-symbols-dark.svg#gh-dark-mode-only)

```kotlin
"Café🇷🇺👍🏽".symbolCount            // 6
"Café🇷🇺👍🏽".takeSymbols(5)         // Café🇷🇺
"Café🇷🇺👍🏽".takeLastSymbols(1)     // 👍🏽
"Café🇷🇺👍🏽".hasFewerSymbolsThan(7) // true
```

`SymbolSequence` walks a text symbol by symbol, for a family that reads it on
its own: `format` finds the words of a name with it.

```kotlin
val symbols = SymbolSequence("Café🇷🇺")
symbols.length         // 5
symbols.extract(3..4)  // é🇷🇺
symbols.isCapitalAt(0) // true
```
