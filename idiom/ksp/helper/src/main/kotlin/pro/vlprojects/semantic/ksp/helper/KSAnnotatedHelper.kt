package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

// KSP's getAnnotationsByType compares a short name and misses an annotation under a type alias.
public fun KSAnnotated.getAnnotation(annotationClass: KClass<out Annotation>): KSAnnotation? =
    this.annotations.firstOrNull { annotation -> annotation.isInstanceOf(annotationClass) }

public fun KSAnnotated.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean =
    this.getAnnotation(annotationClass) != null

private fun KSAnnotation.isInstanceOf(annotationClass: KClass<out Annotation>) = this
    .getAnnotationClass()
    ?.getQualifiedName() == annotationClass.qualifiedName
