package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.getShortName
import pro.vlprojects.semantic.ksp.helper.resolveClassDeclaration

@OptIn(ExperimentalCompilerApi::class)
class KSTypeHelperTests {

    @Test
    fun `the class of a type under a type alias is the class the alias stands for`() = SourceFile
        .kotlin(
            "Report.kt",
            """
                class LedgerRepository
                typealias Ledgers = LedgerRepository
                class Report<T>(val ledgers: Ledgers, val item: T)
            """,
        )
        .let { file -> compileReporting(::reportPropertyClasses, file) }
        .assertReports("Report -> [ledgers LedgerRepository, item null]")

    private fun reportPropertyClasses(declaration: KSClassDeclaration) = declaration
        .getDeclaredProperties()
        .joinToString(transform = ::describePropertyClass)

    private fun describePropertyClass(property: KSPropertyDeclaration) = property.type
        .resolve()
        .resolveClassDeclaration()
        ?.getShortName()
        .let { className -> "${property.simpleName.asString()} $className" }
}
