package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueArgument

/** The annotation class, alias expanded: `Audited` for `@Checked`; `null` if unresolved. */
public fun KSAnnotation.resolveClassDeclaration(): KSClassDeclaration? =
    this.annotationType.resolve().declaration.toClassDeclaration()

/** Types of the class literals in [argumentName]: `[Ledger, Invoice]`, a single one of Java too: `[Ledger]`. */
public fun KSAnnotation.getTypeValues(argumentName: String): List<KSType> = this.arguments
    .filter { argument -> argument.name?.asString() == argumentName }
    .map(KSValueArgument::value)
    .flatMap { value -> value as? List<*> ?: listOf(value) }
    .filterIsInstance<KSType>()

private fun KSDeclaration.toClassDeclaration() = when (this) {
    is KSTypeAlias -> this.findActualType()
    else -> this as? KSClassDeclaration
}
