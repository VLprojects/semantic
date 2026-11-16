package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.findAllNestedClasses
import pro.vlprojects.semantic.ksp.helper.readShortName

@OptIn(ExperimentalCompilerApi::class)
class KSClassDeclarationHelperTests {

    @Test
    fun `nested classes are found at every depth, without the class itself`() = SourceFile
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
        .let { file -> compileReporting(::reportNestedClasses, file) }
        .assertReports(
            "FileModule -> [FileModule.Client, FileModule.Client.Session, FileModule.Companion]",
            "Plain -> []",
        )

    private fun reportNestedClasses(declaration: KSClassDeclaration) = declaration
        .findAllNestedClasses()
        .joinToString(transform = KSClassDeclaration::readShortName)
}
