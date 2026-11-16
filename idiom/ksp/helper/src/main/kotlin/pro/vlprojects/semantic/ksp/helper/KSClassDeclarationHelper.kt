package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * The classes declared directly in this class, the companion included, without the ones nested deeper.
 *
 * `class FileModule { class Client { class Session }; companion object }` gives `[Client, Companion]`.
 */
public fun KSClassDeclaration.getDeclaredClasses(): Sequence<KSClassDeclaration> =
    this.declarations.filterIsInstance<KSClassDeclaration>()
