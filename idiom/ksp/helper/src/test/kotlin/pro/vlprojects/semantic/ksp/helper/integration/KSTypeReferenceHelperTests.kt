package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.findAllInvolvedClasses
import pro.vlprojects.semantic.ksp.helper.readQualifiedName

@OptIn(ExperimentalCompilerApi::class)
class KSTypeReferenceHelperTests {

    @Test
    fun `the classes of the arguments are involved, each once`() = this.assertInvolvedClasses(
        "abstract class Index { abstract val value: Map<String, List<String>> }",
        "Index -> [kotlin.String, kotlin.collections.List, kotlin.collections.Map]",
    )

    @Test
    fun `a type alias involves the classes it stands for`() = this.assertInvolvedClasses(
        "typealias Names = List<String>\nabstract class Roster { abstract val names: Names }",
        "Roster -> [kotlin.String, kotlin.collections.List]",
    )

    @Test
    fun `a bound that leads back to its own parameter is walked once`() = this.assertInvolvedClasses(
        "abstract class Sorter<T : Comparable<T>> { abstract val latest: T }",
        "Sorter -> [kotlin.Comparable]",
    )

    private fun assertInvolvedClasses(source: String, report: String) = SourceFile
        .kotlin("Involved.kt", source)
        .let { file -> compileReporting(::reportInvolvedClasses, file) }
        .assertReports(report)

    private fun reportInvolvedClasses(declaration: KSClassDeclaration) = declaration
        .getDeclaredProperties()
        .map(KSPropertyDeclaration::type)
        .flatMap(KSTypeReference::findAllInvolvedClasses)
        .map(KSClassDeclaration::readQualifiedName)
        .sorted()
        .joinToString()
}
