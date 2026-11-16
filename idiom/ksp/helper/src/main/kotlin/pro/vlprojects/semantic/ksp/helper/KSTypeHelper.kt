package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias

/**
 * The declaration of the class of this type, a type alias expanded, or `null` for a type parameter.
 *
 * ```
 * typealias Ledgers = LedgerRepository
 * @Subjects([Ledgers::class]) class Report
 *
 * subjects.getTypeValues("classes").map(KSType::resolveClassDeclaration) // [LedgerRepository]
 * ```
 */
public fun KSType.resolveClassDeclaration(): KSClassDeclaration? = when (val declaration = this.declaration) {
    is KSTypeAlias -> declaration.findActualType()
    else -> declaration as? KSClassDeclaration
}
