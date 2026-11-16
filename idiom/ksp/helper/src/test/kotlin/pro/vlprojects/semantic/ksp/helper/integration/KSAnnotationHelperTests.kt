package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.getAnnotationClass
import pro.vlprojects.semantic.ksp.helper.getClassLiterals
import pro.vlprojects.semantic.ksp.helper.getQualifiedName

@OptIn(ExperimentalCompilerApi::class)
class KSAnnotationHelperTests {

    @Test
    fun `the class of an annotation under a type alias is the class it stands for`() = SourceFile
        .kotlin("Invoice.kt", "$AUDITED_IMPORT\ntypealias Checked = Audited\n@Checked\nclass Invoice")
        .let { file -> compileReporting(::reportAnnotationClass, file) }
        .assertReports("Invoice -> [pro.vlprojects.semantic.ksp.helper.integration.Audited]")

    @Test
    fun `the class literals of an argument are read from Kotlin and from Java`() {
        val kotlinFile = SourceFile.kotlin(
            "Subjects.kt",
            """
                import kotlin.reflect.KClass

                annotation class Subjects(val classes: Array<KClass<*>>)
                class Ledger
                class Invoice
                @Subjects([Ledger::class, Invoice::class])
                class Tagged
            """,
        )

        val javaFile = SourceFile.java("JavaTagged.java", "@Subjects(classes = Ledger.class)\nclass JavaTagged {}")

        compileReporting(::reportSubjects, kotlinFile, javaFile)
            .assertReports("Tagged -> [Ledger, Invoice]", "JavaTagged -> [Ledger]")
    }

    private fun reportAnnotationClass(declaration: KSClassDeclaration) = declaration.annotations
        .firstOrNull()
        ?.getAnnotationClass()
        ?.getQualifiedName()
        .orEmpty()

    private fun reportSubjects(declaration: KSClassDeclaration) = declaration.annotations
        .firstOrNull()
        ?.getClassLiterals("classes")
        .orEmpty()
        .map(KSType::declaration)
        .joinToString(transform = KSDeclaration::getQualifiedName)

    private companion object {

        const val AUDITED_IMPORT = "import pro.vlprojects.semantic.ksp.helper.integration.Audited"
    }
}
