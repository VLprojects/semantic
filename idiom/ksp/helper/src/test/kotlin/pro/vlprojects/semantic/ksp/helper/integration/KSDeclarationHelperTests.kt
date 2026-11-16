package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.getQualifiedName
import pro.vlprojects.semantic.ksp.helper.getShortName
import pro.vlprojects.semantic.ksp.helper.isSynthetic

@OptIn(ExperimentalCompilerApi::class)
class KSDeclarationHelperTests {

    @Test
    fun `a short name keeps the classes around and drops the package`() = SourceFile
        .kotlin("FileModule.kt", "package files\nclass FileModule { class SharedClient }")
        .let { file -> compileReporting(::reportNames, file) }
        .assertReports("FileModule -> [FileModule.SharedClient, files.FileModule]")

    @Test
    fun `a member that a data class generates is synthetic`() = SourceFile
        .kotlin("FileRow.kt", "data class FileRow(val name: String) { fun describe() = name }")
        .let { file -> compileReporting(::reportSynthetic, file) }
        .assertReports("FileRow -> [component1 true, describe false]")

    private fun reportNames(declaration: KSClassDeclaration) = declaration.declarations
        .filterIsInstance<KSClassDeclaration>()
        .map(KSClassDeclaration::getShortName)
        .plus(declaration.getQualifiedName())
        .joinToString()

    private fun reportSynthetic(declaration: KSClassDeclaration) = declaration
        .getDeclaredFunctions()
        .filter { function -> function.simpleName.asString() in REPORTED_FUNCTIONS }
        .map(::describeSynthetic)
        .sorted()
        .joinToString()

    private fun describeSynthetic(function: KSFunctionDeclaration) = function.simpleName
        .asString()
        .let { name -> "$name ${function.isSynthetic()}" }

    private companion object {

        val REPORTED_FUNCTIONS = setOf("component1", "describe")
    }
}
