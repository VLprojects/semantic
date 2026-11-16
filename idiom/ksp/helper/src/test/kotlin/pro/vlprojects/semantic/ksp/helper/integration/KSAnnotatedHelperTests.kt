package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.hasAnnotation

@OptIn(ExperimentalCompilerApi::class)
class KSAnnotatedHelperTests {

    @Test
    fun `an annotation of the class is found`() = this.assertAudited(
        "@Audited\nclass Ledger\nclass Draft",
        "Ledger -> [true]",
        "Draft -> [false]",
    )

    @Test
    fun `an annotation under a type alias is found`() = this.assertAudited(
        "typealias Checked = Audited\n@Checked\nclass Invoice",
        "Invoice -> [true]",
    )

    @Test
    fun `an annotation of the same short name from another package is not found`() = SourceFile
        .kotlin("Archived.kt", "package archive\nannotation class Audited\n@archive.Audited\nclass Archived")
        .let { file -> compileReporting(::reportAudited, file) }
        .assertReports("Archived -> [false]")

    private fun assertAudited(source: String, vararg reports: String) = SourceFile
        .kotlin("Ledger.kt", "$AUDITED_IMPORT\n$source")
        .let { file -> compileReporting(::reportAudited, file) }
        .assertReports(*reports)

    private fun reportAudited(declaration: KSClassDeclaration) = declaration
        .hasAnnotation(Audited::class)
        .toString()

    private companion object {

        const val AUDITED_IMPORT = "import pro.vlprojects.semantic.ksp.helper.integration.Audited"
    }
}
