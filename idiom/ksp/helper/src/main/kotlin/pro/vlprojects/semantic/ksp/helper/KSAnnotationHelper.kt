package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueArgument

/**
 * The declaration of the class of this annotation, a type alias expanded, or `null` if it does not resolve.
 *
 * With `typealias Checked = Audited`, `@Checked` gives the declaration of `Audited`.
 */
public fun KSAnnotation.resolveClassDeclaration(): KSClassDeclaration? =
    this.annotationType.resolve().declaration.toClassDeclaration()

/**
 * The types in the value of the argument [argumentName], one per class literal.
 *
 * `@Subjects(classes = [Ledger::class, Invoice::class])` gives `[Ledger, Invoice]` for `"classes"`.
 * Java passes a single value of an array without braces: `@Subjects(classes = Ledger.class)` gives `[Ledger]`.
 */
public fun KSAnnotation.getTypeValues(argumentName: String): List<KSType> = this.arguments
    .filter { argument -> argument.name?.asString() == argumentName }
    .map(KSValueArgument::value)
    .flatMap { value -> value as? List<*> ?: listOf(value) }
    .filterIsInstance<KSType>()

private fun KSDeclaration.toClassDeclaration() = when (this) {
    is KSTypeAlias -> this.findActualType()
    else -> this as? KSClassDeclaration
}
