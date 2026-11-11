package pro.vlprojects.semantic.pattern.behavioral.resolver.error

public class NotResolvedException(
    public val context: Any?,
) : NoSuchElementException("Nothing is resolved for [context: $context]")
