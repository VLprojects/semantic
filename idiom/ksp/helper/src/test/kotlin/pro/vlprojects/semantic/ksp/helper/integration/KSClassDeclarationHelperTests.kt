package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.getDeclaredClasses
import pro.vlprojects.semantic.ksp.helper.getShortName

@OptIn(ExperimentalCompilerApi::class)
class KSClassDeclarationHelperTests {

    @Test
    fun `declared classes are the direct ones, without the class itself`() = SourceFile
        .kotlin(
            "FileModule.kt",
            """
                class FileModule {
                    class Client {
                        class Session
                    }
                    companion object
                }
                class Plain
            """,
        )
        .let { file -> compileReporting(::reportDeclaredClasses, file) }
        .assertReports(
            "FileModule -> [FileModule.Client, FileModule.Companion]",
            "Plain -> []",
        )

    private fun reportDeclaredClasses(declaration: KSClassDeclaration) = declaration
        .getDeclaredClasses()
        .joinToString(transform = KSClassDeclaration::getShortName)
}
