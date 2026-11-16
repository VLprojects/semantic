package pro.vlprojects.semantic.ksp.helper.integration

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ksp.helper.findAnnotationClass
import pro.vlprojects.semantic.ksp.helper.readArgumentValues
import pro.vlprojects.semantic.ksp.helper.readQualifiedName

@OptIn(ExperimentalCompilerApi::class)
class KSAnnotationHelperTests {

    @Test
    fun `the class of an annotation under a type alias is the class it stands for`() = SourceFile
        .kotlin("Aliased.kt", "typealias Obsolete = Deprecated\n@Obsolete(\"old\") class Aliased")
        .let { file -> compileReporting(::reportAnnotationClass, file) }
        .assertReports("Aliased -> [kotlin.Deprecated]")

    @Test
    fun `the values of an array attribute are read from Kotlin and from Java`() {
        val kotlinFile = SourceFile.kotlin(
            "Tags.kt",
            "annotation class Tags(val names: Array<String>)\n@Tags([\"files\", \"billing\"]) class Tagged",
        )

        val javaFile = SourceFile.java("JavaTagged.java", "@Tags(names = \"files\")\nclass JavaTagged {}")

        compileReporting(::reportNames, kotlinFile, javaFile)
            .assertReports("Tagged -> [files, billing]", "JavaTagged -> [files]")
    }

    private fun reportAnnotationClass(declaration: KSClassDeclaration) = declaration.annotations
        .firstOrNull()
        ?.findAnnotationClass()
        ?.readQualifiedName()
        .orEmpty()

    private fun reportNames(declaration: KSClassDeclaration) = declaration.annotations
        .firstOrNull()
        ?.readArgumentValues("names")
        .orEmpty()
        .joinToString()
}
