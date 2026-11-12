package pro.vlprojects.semantic.pattern.behavioral.resolver

internal fun checkHasAllowedPackage(allowedPackages: Set<String>) {
    require(allowedPackages.isNotEmpty()) { "Allowed packages are empty" }
}

internal fun checkHasOnlyPackageNames(allowedPackages: Set<String>) {
    val invalidNames = allowedPackages
        .filterNot(String::isPackageName)
        .map { name -> "'$name'" }

    require(invalidNames.isEmpty()) { "Allowed packages are not package names: $invalidNames" }
}
