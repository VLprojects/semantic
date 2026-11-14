package pro.vlprojects.semantic.pattern.behavioral.resolver.error

import java.util.Collections

public class ClassNotAllowedException(
    public val className: String,
    allowedPackages: Set<String>,
) : IllegalArgumentException("The class '$className' is not in the allowed packages: $allowedPackages") {

    public val allowedPackages: Set<String> = allowedPackages
        .toSet()
        .let(Collections::unmodifiableSet)
}
