package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * The classes declared directly in this class, the companion included.
 *
 * ```
 * class FileModule { class Client { class Session }; companion object }
 *
 * fileModule.getDeclaredClasses() // [Client, Companion]
 * ```
 */
public fun KSClassDeclaration.getDeclaredClasses(): Sequence<KSClassDeclaration> =
    this.declarations.filterIsInstance<KSClassDeclaration>()
