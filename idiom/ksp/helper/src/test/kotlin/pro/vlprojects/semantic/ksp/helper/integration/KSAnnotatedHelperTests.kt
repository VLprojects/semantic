package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.findAnnotation
import pro.vlprojects.semantic.ksp.helper.hasAnnotation

@OptIn(ExperimentalCompilerApi::class)
class KSAnnotatedHelperTests {

    @Test
    fun `an annotation of the class is found`() = SourceFile
        .kotlin("Legacy.kt", "@Deprecated(\"old\") class Legacy\nclass Current")
        .let { file -> compileReporting(::reportDeprecated, file) }
        .assertReports("Legacy -> [true]", "Current -> [false]")

    @Test
    fun `an annotation under a type alias is found under the name it is written with`() = SourceFile
        .kotlin("Aliased.kt", "typealias Obsolete = Deprecated\n@Obsolete(\"old\") class Aliased")
        .let { file -> compileReporting(::reportDeprecatedName, file) }
        .assertReports("Aliased -> [Obsolete]")

    @Test
    fun `an annotation of the same short name from another package is not found`() = SourceFile
        .kotlin("Archived.kt", "package legacy\nannotation class Deprecated\n@legacy.Deprecated class Archived")
        .let { file -> compileReporting(::reportDeprecated, file) }
        .assertReports("Archived -> [false]")

    private fun reportDeprecated(declaration: KSClassDeclaration) = declaration
        .hasAnnotation(Deprecated::class)
        .toString()

    private fun reportDeprecatedName(declaration: KSClassDeclaration) = declaration
        .findAnnotation(Deprecated::class)
        ?.shortName
        ?.asString()
        .orEmpty()
}
