# semantic-format

How a value is written down: a name in cases, a long value abbreviated, a
secret masked. The words of a name and the symbols of a text come from
`semantic-helper`.

| Package | Extends | What is there |
| --- | --- | --- |
| `format` | `String`, `WordSequence`, `Any`, `Int`, `Long`, later `Duration` | cases, the class name of a value in kebab case, plurals |
| `format.abbreviation` | `String` | abbreviation |
| `format.mask` | `String` | masking |

## Cases

A name splits into words, and the words join back in any case.

![A name split into words and joined in each case](../../docs/assets/format-cases-light.svg#gh-light-mode-only)
![A name split into words and joined in each case](../../docs/assets/format-cases-dark.svg#gh-dark-mode-only)

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

`kebabClassName` names a value after its class in kebab case, as an object
names itself: a bounded context, a subdomain, the type of an event.

```kotlin
object ShoppingCartItem

ShoppingCartItem.kebabClassName // shopping-cart-item
```

`capitalizeFirstLetter` starts a value with a capital, `capitalizeEachPart`
each part of a name; the rest keeps its case.

```kotlin
"order id".capitalizeFirstLetter() // Order id
"jean-luc".capitalizeEachPart("-") // Jean-Luc
```

## Abbreviation

`abbreviated` keeps a few symbols of a long value visible and puts an ellipsis
in place of the rest: the prefix, the infix or the suffix. The abbreviation
lives in the package `format.abbreviation`.

![An identifier cut in the middle, at the beginning and at the end](../../docs/assets/format-abbreviated-light.svg#gh-light-mode-only)
![An identifier cut in the middle, at the beginning and at the end](../../docs/assets/format-abbreviated-dark.svg#gh-dark-mode-only)

```kotlin
id.abbreviated()                                   // 019…1d
id.abbreviated(visibleLength = 8)                  // 0190…c41d
id.abbreviated(abbreviation = PrefixAbbreviation) // …ec41d
id.abbreviated(abbreviation = { source, length -> "${source.take(length)}…" })
```

A value shorter than `minLength`, 10 by default, stays whole.

## Masking

`masked` keeps a few letters and digits of a value visible and masks the rest:
the prefix, the infix or the suffix. The separators stay, so a phone or a card
still reads as one. The masking lives in the package `format.mask`; a value
object with rules of its own, an email, writes a masking of its own.

```kotlin
"4242 4242 4242 4242".masked()                                          // 42** **** **** **42
"4242 4242 4242 4242".masked(visibleLength = 6, masking = SuffixMasking) // 4242 42** **** ****
"+7 (916) 123-45-67".masked(visibleLength = 3, masking = InfixMasking)   // +7 (9**) ***-**-*7
"secret".masked(visibleLength = 0, maskChar = '•')                       // ••••••
```

By default the mask is as long as the masked part. `maskLength = 3` makes it
`***` whatever the value, so the length gives nothing away:

```kotlin
"4242 4242 4242 4242".masked(maskLength = 3)                          // 42***42
"4242 4242 4242 4242".masked(masking = PrefixMasking, maskLength = 3) // *** 4242
```

A value too short to mask anything is masked whole.

## Plurals

`pluralize` picks the form of a word for a count by the rules of Russian: one,
few or many; `pluralizeWithCount` writes the count before it.

```kotlin
21.pluralize("день", "дня", "дней")          // день
21.pluralizeWithCount("день", "дня", "дней") // 21 день
```
