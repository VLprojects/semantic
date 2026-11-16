package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile

internal class ReportingSymbolProcessor(
    private val logger: KSPLogger,
    private val report: (KSClassDeclaration) -> String,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getNewFiles()
            .flatMap(KSFile::declarations)
            .filterIsInstance<KSClassDeclaration>()
            .forEach(::reportClass)

        return emptyList()
    }

    private fun reportClass(declaration: KSClassDeclaration) {
        val name = declaration.simpleName.asString()
        val text = this.report(declaration)

        this.logger.warn("$name -> [$text]")
    }
}
