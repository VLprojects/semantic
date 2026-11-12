package pro.vlprojects.semantic.pattern.behavioral.resolver

internal fun checkHasTrustedPackage(trustedPackages: Set<String>) {
    require(trustedPackages.isNotEmpty()) { "Trusted packages are empty" }
}

internal fun checkHasNoInvalidPackageName(trustedPackages: Set<String>) {
    val invalidNames = trustedPackages
        .filterNot(String::isPackageName)
        .map { name -> "'$name'" }

    require(invalidNames.isEmpty()) { "Trusted packages are not package names: $invalidNames" }
}
