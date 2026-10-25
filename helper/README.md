# semantic-helper

Extensions of the types of Kotlin and the JDK that the other families share.
Plain Kotlin and the JDK, no dependencies.

Each group of helpers is a package under `pro.vlprojects.semantic.helper`, a
file per type it extends: `StringHelper.kt`, `InstantHelper.kt`. A file of `format` is
named `<Type>FormatHelper.kt`, so Java does not see two `StringHelperKt`.

| Package | Extends | What is there |
| --- | --- | --- |
| `text` | `String` | the text itself: symbols as a reader sees them |
| `format` | `String`, later `Duration` and numbers | how a value is written down: words, cases, abbreviation |
| `time` | `Instant`, `LocalDate`, `Duration` | planned |
| `number` | `BigDecimal`, `Int`, `Long` | planned |
| `lang` | `Any`, `KClass` | planned |

A helper extends a type that already exists and knows nothing of the domain:
what speaks of aggregates or value objects belongs to its family.

## Cases

A name splits into words, and the words join back in any case.

![A name split into words and joined in kebab, snake and camel case](../docs/assets/helper-cases-light.svg#gh-light-mode-only)
![A name split into words and joined in kebab, snake and camel case](../docs/assets/helper-cases-dark.svg#gh-dark-mode-only)

```kotlin
"ShoppingCartItem".toKebabCase() // shopping-cart-item
"order_id".toCamelCase()         // orderId
```

## Words

A word starts where the case changes, after a separator or after a digit.

![Where a name breaks into words](../docs/assets/helper-words-light.svg#gh-light-mode-only)
![Where a name breaks into words](../docs/assets/helper-words-dark.svg#gh-dark-mode-only)

`toWordSequence()` gives the words; `WordSequence` is built as `CharSequence`
is, so a service with rules of its own, `iPhone` as one word, implements it and
gets the cases too. `Word.of` takes letters and digits only.

```kotlin
listOf(Word.of("iPhone"), Word.of("Case")).toWordSequence().toKebabCase() // iphone-case
```

## Abbreviation

`abbreviated` keeps a few symbols of a long value and hides the rest behind an
ellipsis, where the mode says.

![An identifier cut in the middle, at the beginning and at the end](../docs/assets/helper-abbreviated-light.svg#gh-light-mode-only)
![An identifier cut in the middle, at the beginning and at the end](../docs/assets/helper-abbreviated-dark.svg#gh-dark-mode-only)

```kotlin
id.abbreviated()                                  // 019…1d
id.abbreviated(keepLength = 8)                    // 0190…c41d
id.abbreviated(mode = EllipsisMode.HideBeginning) // …ec41d
id.abbreviated(mode = { value, keep -> "${value.take(keep)}…" })
```

A value shorter than `minLength`, 10 by default, stays whole.

## Symbols

A symbol is what a reader sees as one character: a letter with its accent, a
flag, an emoji of several characters. Lengths in the helpers count symbols, and
a cut never splits one.

![Symbols over the chars that store them](../docs/assets/helper-symbols-light.svg#gh-light-mode-only)
![Symbols over the chars that store them](../docs/assets/helper-symbols-dark.svg#gh-dark-mode-only)

```kotlin
"Café🇷🇺👍🏽".symbolCount      // 6
"Café🇷🇺👍🏽".takeSymbols(5)   // Café🇷🇺
"Café🇷🇺👍🏽".takeLastSymbols(1) // 👍🏽
```

## Predicates

A predicate names what a check means, so a condition reads as a sentence.

```kotlin
name.filterNot(Char::isHyphen)
pattern.any(Char::isWildcard)
key.notStartsWithAny("internal.", "debug.")
"Café".isNotAscii() // true
```
