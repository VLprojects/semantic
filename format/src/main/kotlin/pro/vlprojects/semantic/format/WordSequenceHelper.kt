package pro.vlprojects.semantic.format

public fun WordSequence.asIterable(): Iterable<Word> = Iterable(this::iterator)

public fun WordSequence.asSequence(): Sequence<Word> = Sequence(this::iterator)

public fun Collection<Word>.toWordSequence(): WordSequence = this
    .toList()
    .let(::ListWordSequence)
