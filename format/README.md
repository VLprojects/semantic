# semantic-format

How a value is written down: a name in cases, a long value abbreviated, a
secret masked. The words of a name and the symbols of a text come from
`semantic-helper`.

| Package | Extends | What is there |
| --- | --- | --- |
| `format` | `String`, `WordSequence`, later `Duration` and numbers | cases, abbreviation |
| `format.mask` | `String` | masking |

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

## Masking

`masked` hides a value for a log or a screen and keeps its length: the letters
and digits turn into a mask, the separators stay, so a phone or a card still
reads as one. The mode, in the package `format.mask`, says what stays in
sight; a value object with rules of its own, an email, writes a mode of its own.

```kotlin
"4242 4242 4242 4242".masked()                                             // **** **** **** 4242
"4242 4242 4242 4242".masked(HideMiddle.of(keepFirst = 6, keepLast = 4)) // 4242 42** **** 4242
"+7 (916) 123-45-67".masked(HideMiddle.of(keepFirst = 1, keepLast = 2))  // +7 (***) ***-**-67
"secret".masked(HideAll, mask = '•')                                      // ••••••
```

A value too short to hide anything is hidden whole.
