package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

/** The first annotation of [annotationClass] by qualified name, alias included: `@Checked` for `Audited::class`. */
public fun KSAnnotated.getFirstAnnotationOrNull(annotationClass: KClass<out Annotation>): KSAnnotation? =
    this.annotations.firstOrNull { annotation -> annotation.isInstanceOf(annotationClass) }

/** Whether [getFirstAnnotationOrNull] finds an annotation of [annotationClass]. */
public fun KSAnnotated.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean =
    this.getFirstAnnotationOrNull(annotationClass) != null

private fun KSAnnotation.isInstanceOf(annotationClass: KClass<out Annotation>) = this
    .resolveClassDeclaration()
    ?.getQualifiedName() == annotationClass.qualifiedName
