package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

// KSP's getAnnotationsByType compares a short name and misses an annotation under a type alias.
public fun KSAnnotated.findAnnotation(annotationClass: KClass<out Annotation>): KSAnnotation? = this.annotations
    .firstOrNull { annotation -> annotation.isOf(annotationClass) }

public fun KSAnnotated.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean =
    this.findAnnotation(annotationClass) != null

private fun KSAnnotation.isOf(annotationClass: KClass<out Annotation>) = this
    .findAnnotationClass()
    ?.readQualifiedName() == annotationClass.qualifiedName
