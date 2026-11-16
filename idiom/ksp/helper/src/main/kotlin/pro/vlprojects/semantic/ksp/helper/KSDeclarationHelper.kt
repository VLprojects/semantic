package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Origin

/** The qualified name, `files.FileModule.Client`, or an empty string for a local declaration. */
public fun KSDeclaration.getQualifiedName(): String = this.qualifiedName?.asString().orEmpty()

/** The name without the package: `FileModule.Client` for `files.FileModule.Client`. */
public fun KSDeclaration.getShortName(): String = this.packageName
    .asString()
    .let { packageName -> this.qualifiedName?.asString()?.removePrefix("$packageName.") }
    ?: this.simpleName.asString()

/**
 * Whether the compiler generated this declaration without a source of its own.
 *
 * `data class FileRow(val name: String)` gives `true` for `component1()`, `false` for a function written in the class.
 */
public fun KSDeclaration.isSynthetic(): Boolean = this.origin == Origin.SYNTHETIC
