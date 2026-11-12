package pro.vlprojects.semantic.pattern.behavioral.resolver.error

import java.util.Collections

public class ClassNotAllowedException(
    public val className: String,
    trustedPackages: Set<String>,
) : IllegalArgumentException("The class '$className' is not in the trusted packages: $trustedPackages") {

    public val trustedPackages: Set<String> = trustedPackages
        .toSet()
        .let(Collections::unmodifiableSet)
}
