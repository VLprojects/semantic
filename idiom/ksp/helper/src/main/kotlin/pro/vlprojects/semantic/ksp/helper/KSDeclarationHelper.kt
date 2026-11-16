package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Origin

public fun KSDeclaration.readQualifiedName(): String = this.qualifiedName
    ?.asString()
    .orEmpty()

public fun KSDeclaration.readShortName(): String = this.packageName
    .asString()
    .let { packageName -> this.qualifiedName?.asString()?.removePrefix("$packageName.") }
    ?: this.simpleName.asString()

// The compiler generates such a declaration without a source of its own: a member of a data class or of a delegate.
public fun KSDeclaration.isSynthetic(): Boolean = this.origin == Origin.SYNTHETIC
