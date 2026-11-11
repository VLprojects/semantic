package pro.vlprojects.semantic.pattern.behavioral.resolver.error

public class MultipleResolvedException(
    public val context: Any?,
    public val candidates: Collection<Any>,
) : IllegalArgumentException(
        "Several candidates are resolved for [context: $context, candidates: ${candidates.joinToString()}]",
    )
