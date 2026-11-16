package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration

public fun KSClassDeclaration.getNestedClasses(): Sequence<KSClassDeclaration> =
    this.declarations.filterIsInstance<KSClassDeclaration>()
