# semantic-format

How a value is written down: a name in cases, a long value abbreviated. The
words of a name and the symbols of a text come from `semantic-helper`.

| Package | Extends | What is there |
| --- | --- | --- |
| `format` | `String`, `WordSequence`, `Int`, `Long`, later `Duration` | cases, abbreviation, plurals |

## Cases

A name splits into words, and the words join back in any case.

![A name split into words and joined in each case](../docs/assets/format-cases-light.svg#gh-light-mode-only)
![A name split into words and joined in each case](../docs/assets/format-cases-dark.svg#gh-dark-mode-only)

```kotlin
"ShoppingCartItem".toKebabCase()  // shopping-cart-item
"order_id".toCamelCase()          // orderId
"order_id".toConstantCase()       // ORDER_ID
"orderLine".toSentenceCase()      // Order line
```

The cases take a `WordSequence` too, so a name split by rules of its own keeps
them: `iPhone` as one word.

```kotlin
listOf(Word.of("iPhone"), Word.of("Case")).toWordSequence().toKebabCase() // iphone-case
```

`capitalizeFirstLetter` starts a value with a capital, `capitalizeEachPart`
each part of a name; the rest keeps its case.

```kotlin
"order id".capitalizeFirstLetter() // Order id
"jean-luc".capitalizeEachPart("-") // Jean-Luc
```

## Abbreviation

`abbreviated` keeps a few symbols of a long value and hides the rest behind an
ellipsis, where the mode says.

![An identifier cut in the middle, at the beginning and at the end](../docs/assets/format-abbreviated-light.svg#gh-light-mode-only)
![An identifier cut in the middle, at the beginning and at the end](../docs/assets/format-abbreviated-dark.svg#gh-dark-mode-only)

```kotlin
id.abbreviated()                                  // 019…1d
id.abbreviated(keepLength = 8)                    // 0190…c41d
id.abbreviated(mode = EllipsisMode.HideBeginning) // …ec41d
id.abbreviated(mode = { value, keep -> "${value.take(keep)}…" })
```

A value shorter than `minLength`, 10 by default, stays whole.

## Plurals

`pluralize` picks the form of a word for a count by the rules of Russian: one,
few or many; `pluralizeWithCount` writes the count before it.

```kotlin
21.pluralize("день", "дня", "дней")          // день
21.pluralizeWithCount("день", "дня", "дней") // 21 день
```
