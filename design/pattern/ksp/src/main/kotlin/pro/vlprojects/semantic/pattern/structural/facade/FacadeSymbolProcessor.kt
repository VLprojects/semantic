package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import pro.vlprojects.semantic.ksp.helper.getDeclaredClasses

internal class FacadeSymbolProcessor(logger: KSPLogger) : SymbolProcessor {

    private val singleSubsystemMembershipChecker = SingleSubsystemMembershipChecker(logger)

    private val facadeOnClassChecker = FacadeOnClassChecker(logger)

    private val facadeSubsystemsNotEmptyChecker = FacadeSubsystemsNotEmptyChecker(logger)

    private val subsystemAnnotationPresenceChecker = SubsystemAnnotationPresenceChecker(logger)

    private val facadeOutsideSubsystemsChecker = FacadeOutsideSubsystemsChecker(logger)

    private val subsystemTypesHiddenByFacadeChecker = SubsystemTypesHiddenByFacadeChecker(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getNewFiles()
            .flatMap(KSFile::declarations)
            .filterIsInstance<KSClassDeclaration>()
            .flatMap(::flattenClasses)
            .forEach(::checkClass)

        return emptyList()
    }

    private fun checkClass(declaration: KSClassDeclaration) {
        this.singleSubsystemMembershipChecker.check(declaration)

        FacadeAnnotation
            .find(declaration)
            ?.let { annotation -> this.checkFacade(declaration, annotation) }
    }

    private fun checkFacade(declaration: KSClassDeclaration, annotation: FacadeAnnotation) {
        this.facadeOnClassChecker.check(declaration, annotation)
        this.facadeSubsystemsNotEmptyChecker.check(declaration, annotation)
        this.subsystemAnnotationPresenceChecker.check(declaration, annotation)
        this.facadeOutsideSubsystemsChecker.check(declaration)
        this.subsystemTypesHiddenByFacadeChecker.check(declaration, annotation)
    }

    private fun flattenClasses(declaration: KSClassDeclaration): Sequence<KSClassDeclaration> = declaration
        .getDeclaredClasses()
        .flatMap(::flattenClasses)
        .let { nested -> sequenceOf(declaration) + nested }
}
