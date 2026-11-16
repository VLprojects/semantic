package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

/**
 * The first annotation of [annotationClass], also one under a type alias, or `null`.
 *
 * ```
 * typealias Checked = Audited
 * @Checked class Invoice
 *
 * invoice.getFirstAnnotationOrNull(Audited::class) // @Checked
 * ```
 */
public fun KSAnnotated.getFirstAnnotationOrNull(annotationClass: KClass<out Annotation>): KSAnnotation? =
    this.annotations.firstOrNull { annotation -> annotation.isInstanceOf(annotationClass) }

/**
 * Whether this symbol carries an annotation of [annotationClass], also under a type alias.
 *
 * ```
 * typealias Checked = Audited
 * @Checked class Invoice
 *
 * invoice.hasAnnotation(Audited::class) // true
 * ```
 */
public fun KSAnnotated.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean =
    this.getFirstAnnotationOrNull(annotationClass) != null

private fun KSAnnotation.isInstanceOf(annotationClass: KClass<out Annotation>) = this
    .resolveClassDeclaration()
    ?.getQualifiedName() == annotationClass.qualifiedName
