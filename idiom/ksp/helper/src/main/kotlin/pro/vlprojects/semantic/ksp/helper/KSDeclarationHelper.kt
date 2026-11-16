package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Origin

/**
 * The qualified name, or an empty string for a local declaration.
 *
 * ```
 * package files
 * class FileModule { class Client }
 *
 * client.getQualifiedName() // files.FileModule.Client
 * ```
 */
public fun KSDeclaration.getQualifiedName(): String = this.qualifiedName?.asString().orEmpty()

/**
 * The name without the package.
 *
 * ```
 * package files
 * class FileModule { class Client }
 *
 * client.getShortName() // FileModule.Client
 * ```
 */
public fun KSDeclaration.getShortName(): String = this.packageName
    .asString()
    .let { packageName -> this.qualifiedName?.asString()?.removePrefix("$packageName.") }
    ?: this.simpleName.asString()

/**
 * Whether the compiler generated this declaration without a source of its own.
 *
 * ```
 * data class FileRow(val name: String) { fun describe() = name }
 *
 * component1.isSynthetic() // true
 * describe.isSynthetic()   // false
 * ```
 */
public fun KSDeclaration.isSynthetic(): Boolean = this.origin == Origin.SYNTHETIC
