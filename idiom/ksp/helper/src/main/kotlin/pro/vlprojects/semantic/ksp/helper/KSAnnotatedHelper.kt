package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

/**
 * The first annotation of [annotationClass] on this symbol, also one under a type alias, or `null`.
 *
 * Compares the qualified name, unlike `getAnnotationsByType` of KSP, which compares the short one:
 * with `typealias Checked = Audited`, `@Checked class Invoice` gives `@Checked` for `Audited::class`.
 */
public fun KSAnnotated.getFirstAnnotationOrNull(annotationClass: KClass<out Annotation>): KSAnnotation? =
    this.annotations.firstOrNull { annotation -> annotation.isInstanceOf(annotationClass) }

/**
 * Whether this symbol carries an annotation of [annotationClass], also under a type alias.
 *
 * With `typealias Checked = Audited`, `@Checked class Invoice` gives `true` for `Audited::class`.
 */
public fun KSAnnotated.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean =
    this.getFirstAnnotationOrNull(annotationClass) != null

private fun KSAnnotation.isInstanceOf(annotationClass: KClass<out Annotation>) = this
    .resolveClassDeclaration()
    ?.getQualifiedName() == annotationClass.qualifiedName
