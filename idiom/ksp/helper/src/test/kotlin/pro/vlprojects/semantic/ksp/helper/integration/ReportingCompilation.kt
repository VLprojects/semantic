package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCompilerApi::class)
internal fun compileReporting(report: (KSClassDeclaration) -> String, vararg files: SourceFile) = KotlinCompilation()
    .apply {
        sources = files.toList()
        inheritClassPath = true
        configureKsp { symbolProcessorProviders += ReportingSymbolProcessorProvider(report) }
    }
    .compile()

@OptIn(ExperimentalCompilerApi::class)
internal fun JvmCompilationResult.assertReports(vararg reports: String) {
    assertEquals(ExitCode.OK, this.exitCode, this.messages)

    reports.forEach { report ->
        assertTrue(report in this.messages) { "No \"$report\" in ${this.messages}" }
    }
}
