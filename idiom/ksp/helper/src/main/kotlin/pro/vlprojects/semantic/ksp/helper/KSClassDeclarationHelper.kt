package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration

/** Direct nested classes, companion included: `class A { class B { class C } }` gives `[B]`. */
public fun KSClassDeclaration.getDeclaredClasses(): Sequence<KSClassDeclaration> =
    this.declarations.filterIsInstance<KSClassDeclaration>()
