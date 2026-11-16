package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import pro.vlprojects.semantic.ksp.helper.getQualifiedName
import pro.vlprojects.semantic.ksp.helper.getReferencedClasses
import pro.vlprojects.semantic.ksp.helper.getShortName
import pro.vlprojects.semantic.ksp.helper.hasAnnotation
import pro.vlprojects.semantic.ksp.helper.resolveClassDeclaration

internal class SubsystemMembership private constructor(
    val annotation: KSAnnotation,
    private val subsystem: KSClassDeclaration,
    private val source: KSClassDeclaration,
    private val aggregateIndex: Int,
) {

    fun getSubsystemName() = this.subsystem.getQualifiedName()

    fun isDirectlyPresent() = this.aggregateIndex == 0

    fun findSymbol(member: KSClassDeclaration) = when {
        this.isDirectlyPresent() -> this.annotation
        else -> member
    }

    fun describe() = when {
        this.isDirectlyPresent() -> "@${this.subsystem.getShortName()}"
        else -> "@${this.subsystem.getShortName()} through ${this.source.getShortName()}"
    }

    companion object Factory {

        fun findAll(declaration: KSClassDeclaration) = this
            .findSources(declaration)
            .flatMapIndexed { aggregateIndex, source -> this.findDirectlyPresent(source, aggregateIndex) }
            .distinctBy(SubsystemMembership::getSubsystemName)

        // A visited class is skipped: an argument of a supertype may lead back to a class already walked.
        private fun findSources(declaration: KSClassDeclaration): List<KSClassDeclaration> {
            val classesByName = declaration
                .getQualifiedName()
                .let { name -> mutableMapOf(name to declaration) }

            val queue = ArrayDeque<KSClassDeclaration>().also { deque -> deque.addLast(declaration) }

            while (queue.isNotEmpty()) {
                queue
                    .removeFirst()
                    .superTypes
                    .flatMap(KSTypeReference::getReferencedClasses)
                    .filterNot { supertype -> supertype.getQualifiedName() in classesByName }
                    .forEach { supertype ->
                        supertype
                            .getQualifiedName()
                            .let { name -> classesByName[name] = supertype }

                        queue.addLast(supertype)
                    }
            }

            return classesByName.values.toList()
        }

        private fun findDirectlyPresent(source: KSClassDeclaration, aggregateIndex: Int) = source.annotations
            .mapNotNull { annotation -> this.createOrNull(annotation, source, aggregateIndex) }
            .toList()

        private fun createOrNull(annotation: KSAnnotation, source: KSClassDeclaration, aggregateIndex: Int) = annotation
            .resolveClassDeclaration()
            ?.takeIf { subsystem -> subsystem.hasAnnotation(Facade.Subsystem::class) }
            ?.let { subsystem -> SubsystemMembership(annotation, subsystem, source, aggregateIndex) }
    }
}
