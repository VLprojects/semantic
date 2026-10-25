# semantic-helper

Extensions of the types of Kotlin and the JDK that the other families share.
Plain Kotlin and the JDK, no dependencies.

Each group of helpers is a package under `pro.vlprojects.semantic.helper`, a
file per type it extends: `StringHelper.kt`, `InstantHelper.kt`.

| Package | Extends | What is there |
| --- | --- | --- |
| `text` | `String`, `CharSequence` | the text itself: symbols as a reader sees them, the words of a name |
| `time` | `Instant`, `LocalDate`, `Duration` | planned |
| `number` | `Int`, `Long`, `Short`, `Byte`, `BigDecimal` | the sign of a number, a narrowing that refuses to lose a digit |
| `lang` | `Any`, `KClass` | planned |

A helper extends a type that already exists and knows nothing of the domain:
what speaks of aggregates or value objects belongs to its family. How a value
is written down, cases and abbreviation, is the [`format`](../format/README.md)
family.

## Words

A word starts where the case changes, after a separator or after a digit.

![Where a name breaks into words](../docs/assets/helper-words-light.svg#gh-light-mode-only)
![Where a name breaks into words](../docs/assets/helper-words-dark.svg#gh-dark-mode-only)

`toWordSequence()` gives the words; `WordSequence` is built as `CharSequence`
is, so a service with rules of its own implements it. `Word.of` takes letters
and digits only.

```kotlin
"ShoppingCartItem".toWordSequence()                         // Shopping Cart Item
listOf(Word.of("iPhone"), Word.of("Case")).toWordSequence() // iPhone Case
```

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

## Predicates

A predicate names what a check means, so a condition reads as a sentence.

```kotlin
name.filterNot(Char::isHyphen)
pattern.any(Char::isWildcard)
key.notStartsWithAny("internal.", "debug.")
"Café".isNotAscii() // true
```

## Cleanup

A value typed by a person comes with doubled separators and accents; a cleanup
brings it to one form before it is compared or stored.

```kotlin
"Anne  \t Marie".collapseSpaces() // Anne Marie
"Jean---Luc".collapseHyphens()    // Jean-Luc
"O''Neil".collapseApostrophes()   // O'Neil
"a..b".collapseDots()             // a.b
"Café crème".removeDiacritics()   // Cafe creme
```

`collapse` turns a run of any characters you choose into one.

```kotlin
"order  42\t\tid".collapse('_', Char::isWhitespace) // order_42_id
"a-_b_-_c".collapse('-', listOf('-', '_'))          // a-b-c
```

## Numbers

The sign of a number reads as a word, and a narrowing to a smaller type refuses
a value that does not fit instead of cutting it.

```kotlin
amount.isPositive()
quantity.isNonNegative()
BigDecimal("42.00").toSafeInt() // 42
BigDecimal("1.5").toSafeInt()   // ArithmeticException
5_000_000_000L.toSafeInt()      // ArithmeticException
```
