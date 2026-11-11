package pro.vlprojects.semantic.pattern.behavioral.resolver.error

public class MultipleCandidatesException(
    public val candidates: Collection<Any>,
) : IllegalArgumentException("Several candidates are found: $candidates")
