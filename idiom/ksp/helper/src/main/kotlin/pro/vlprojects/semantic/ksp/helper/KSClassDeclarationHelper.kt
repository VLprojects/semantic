package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration

public fun KSClassDeclaration.getDeclaredClasses(): Sequence<KSClassDeclaration> =
    this.declarations.filterIsInstance<KSClassDeclaration>()
