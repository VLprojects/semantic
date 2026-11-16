package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class ReportingSymbolProcessorProvider(private val report: (KSClassDeclaration) -> String) :
    SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment) = environment.logger
        .let { logger -> ReportingSymbolProcessor(logger, this.report) }
}
