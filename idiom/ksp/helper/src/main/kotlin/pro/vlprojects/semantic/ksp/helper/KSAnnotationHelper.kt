package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueArgument

/**
 * The declaration of the annotation class, a type alias expanded, or `null` if it does not resolve.
 *
 * ```
 * typealias Checked = Audited
 * @Checked class Invoice
 *
 * checked.resolveClassDeclaration() // Audited
 * ```
 */
public fun KSAnnotation.resolveClassDeclaration(): KSClassDeclaration? =
    this.annotationType.resolve().declaration.toClassDeclaration()

/**
 * The types of the class literals in the argument [argumentName], a single literal of Java included.
 *
 * ```
 * @Subjects(classes = [Ledger::class, Invoice::class]) class Report
 * @Subjects(classes = Ledger.class) class JavaReport // Java
 *
 * subjects.getTypeValues("classes") // [Ledger, Invoice] and [Ledger]
 * ```
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
