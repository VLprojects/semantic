package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile

internal class FacadeSymbolProcessor(logger: KSPLogger) : SymbolProcessor {

    private val facadeChecker = FacadeChecker(logger)

    private val clientChecker = ClientChecker(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getNewFiles()
            .flatMap(KSFile::declarations)
            .filterIsInstance<KSClassDeclaration>()
            .flatMap(KSClassDeclaration::flattenClasses)
            .forEach(::checkClass)

        return emptyList()
    }

    private fun checkClass(declaration: KSClassDeclaration) {
        when (val annotation = declaration.findFacadeAnnotation()) {
            null -> this.clientChecker.check(declaration)
            else -> this.facadeChecker.check(declaration, annotation)
        }
    }
}
