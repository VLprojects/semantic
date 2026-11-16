package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

internal class FacadeSymbolProcessor(logger: KSPLogger) : SymbolProcessor {

    private val subsystemTypeChecker = SubsystemTypeChecker(logger)

    private val facadeChecker = FacadeChecker(logger, this.subsystemTypeChecker)

    private val clientChecker = ClientChecker(logger, this.subsystemTypeChecker)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getNewFiles()
            .flatMap(KSFile::declarations)
            .forEach(::checkDeclaration)

        return emptyList()
    }

    private fun checkDeclaration(declaration: KSDeclaration) {
        when (declaration) {
            is KSClassDeclaration -> declaration.flattenClasses().forEach(::checkClass)
            is KSFunctionDeclaration -> this.clientChecker.check(declaration)
            is KSPropertyDeclaration -> this.clientChecker.check(declaration)
            else -> Unit
        }
    }

    private fun checkClass(declaration: KSClassDeclaration) {
        when (val annotation = declaration.findFacadeAnnotation()) {
            null -> this.clientChecker.check(declaration)
            else -> this.facadeChecker.check(declaration, annotation)
        }
    }
}
