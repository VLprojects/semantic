package pro.vlprojects.semantic.pattern.behavioral.resolver.error

public class CandidateNotFoundException(
    public val context: Any?,
) : NoSuchElementException("No candidate is found for '$context'")
