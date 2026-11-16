package pro.vlprojects.semantic.pattern.integration.structural.facade

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import pro.vlprojects.semantic.pattern.structural.facade.FacadeSymbolProcessorProvider
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class FacadeSymbolProcessorTests {

    @Test
    fun `facades and subsystems of Kotlin and of Java compile`() {
        val javaFile = SourceFile.java(
            "JavaArchive.java",
            """
                import pro.vlprojects.semantic.pattern.structural.facade.Facade;

                @Facade.Subsystem
                @interface ArchiveSubsystem {}
                @ArchiveSubsystem
                class ArchiveEntity {}
                @Facade(subsystems = ArchiveSubsystem.class)
                class ArchiveFacade {}
                @Facade(subsystems = {FileSubsystem.class, BillingSubsystem.class})
                class ReportFacade {}
            """,
        )

        val result = this.compileSources(javaFile)

        assertEquals(ExitCode.OK, result.exitCode, result.messages)
    }

    @Test
    fun `classes of Java follow the same rules`() {
        val javaFile = SourceFile.java(
            "JavaFacades.java",
            """
                import pro.vlprojects.semantic.pattern.structural.facade.Facade;

                @Facade(subsystems = Plain.class)
                class JavaPlainFacade {}
                @Facade(subsystems = {FileSubsystem.class, Plain.class})
                class JavaFileFacade {}
                @Facade(subsystems = FileSubsystem.class)
                interface JavaFileOperations {}
                @Facade(subsystems = FileSubsystem.class)
                @FileSubsystem
                class JavaMarkedFacade {}
                class JavaFileModule {
                    @FileSubsystem
                    @BillingSubsystem
                    static class SharedClient {}
                }
            """,
        )

        SourceFile
            .kotlin("Plain.kt", "annotation class Plain")
            .let { kotlinFile -> this.compileSources(kotlinFile, javaFile) }
            .let { result ->
                this.assertCompilationFails(
                    result,
                    "JavaFacades.java:3: Facade JavaPlainFacade names @Plain, which is not marked",
                    "JavaFacades.java:5: Facade JavaFileFacade names @Plain, which is not marked",
                    "JavaFacades.java:7: @Facade goes on a class, not on the interface JavaFileOperations",
                    "JavaFacades.java:10: Facade JavaMarkedFacade carries @FileSubsystem: a facade stands outside",
                    "JavaFacades.java:14: JavaFileModule.SharedClient carries the markers of several subsystems",
                )
            }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "val entity: FileEntity get() = TODO()",
            "fun find(): FileEntity = TODO()",
            "fun save(file: FileEntity) = Unit",
            "fun FileEntity.rename() = Unit",
            "val FileEntity.label: String get() = TODO()",
            "fun findAll(): List<FileEntity> = TODO()",
            "fun observe(listener: (FileEntity) -> Unit) = Unit",
            "fun <T : FileEntity> findTyped(): T = TODO()",
            "fun <T : List<FileEntity>> findAll(): T = TODO()",
            "fun <T : FileEntity, R : T> findTyped(): R = TODO()",
            "fun <T : PersistedFile> findTyped(): T = TODO()",
            "fun find() = FileEntity()",
            "companion object { fun find(): FileEntity = TODO() }",
            "internal fun find(): FileEntity = TODO()",
        ],
    )
    fun `a member that a client sees exposes no type of a subsystem`(member: String) = this.assertCompilationFails(
        "@Facade(FileSubsystem::class)\nclass FileReportFacade {\n$member\n}",
        4,
        "Facade FileReportFacade exposes FileEntity of @FileSubsystem",
    )

    @Test
    fun `a property of the constructor counts`() = this.assertCompilationFails(
        "@Facade(FileSubsystem::class)\nclass FileReportFacade(val file: FileEntity)",
        3,
        "Facade FileReportFacade exposes FileEntity of @FileSubsystem",
    )

    @Test
    fun `a type alias counts as the type it stands for`() = this.assertCompilationFails(
        "@Facade(FileSubsystem::class)\nclass FileAliasFacade {\nfun find(): PersistedFile = TODO()\n}",
        4,
        "Facade FileAliasFacade exposes FileEntity of @FileSubsystem",
    )

    @Test
    fun `a nested facade is checked`() = this.assertCompilationFails(
        """
            class Files {
                @Facade(FileSubsystem::class)
                class NestedFacade {
                    fun find(): FileEntity = TODO()
                }
            }
        """.trimIndent(),
        5,
        "Facade Files.NestedFacade exposes FileEntity of @FileSubsystem",
    )

    @Test
    fun `a protected member is not seen by a client`() {
        val result = this.compile(
            """
                @Facade(FileSubsystem::class)
                abstract class FileBaseFacade {
                    protected fun find(): FileEntity = TODO()
                }
            """,
        )

        assertEquals(ExitCode.OK, result.exitCode, result.messages)
    }

    @Test
    fun `a member of a private companion is not seen by a client`() {
        val result = this.compile(
            """
                @Facade(FileSubsystem::class)
                class FileReportFacade {
                    private companion object {
                        fun find(): FileEntity = TODO()
                    }
                }
            """,
        )

        assertEquals(ExitCode.OK, result.exitCode, result.messages)
    }

    @Test
    fun `a bound that leads back to its own parameter is walked once`() {
        val result = this.compile(
            "@Facade(FileSubsystem::class)\nclass SortFacade {\nfun <T : Comparable<T>> findLatest(): T = TODO()\n}",
        )

        assertEquals(ExitCode.OK, result.exitCode, result.messages)
    }

    @Test
    fun `a private method of Java is not seen by a client`() {
        val javaFile = SourceFile.java(
            "JavaPrivateFacade.java",
            """
                import pro.vlprojects.semantic.pattern.structural.facade.Facade;

                @Facade(subsystems = FileSubsystem.class)
                class JavaPrivateFacade {
                    private FileEntity load() { return null; }
                }
            """,
        )

        val result = this.compileSources(javaFile)

        assertEquals(ExitCode.OK, result.exitCode, result.messages)
    }

    @Test
    fun `a method of Java that a client sees exposes no type of a subsystem`() {
        val javaFile = SourceFile.java(
            "JavaFileFacade.java",
            """
                import pro.vlprojects.semantic.pattern.structural.facade.Facade;

                @Facade(subsystems = FileSubsystem.class)
                class JavaFileFacade {
                    FileEntity find() { return null; }
                    protected FileEntity remove() { return null; }
                }
            """,
        )

        this
            .compileSources(javaFile)
            .let { result ->
                this.assertCompilationFails(
                    result,
                    "JavaFileFacade.java:5: Facade JavaFileFacade exposes FileEntity of @FileSubsystem",
                    "JavaFileFacade.java:6: Facade JavaFileFacade exposes FileEntity of @FileSubsystem",
                )
            }
    }

    @Test
    fun `a marker of another module is read from its class file`() {
        val subsystemsModule = this.compileSources()

        val facade = SourceFile.kotlin(
            "RemoteFileFacade.kt",
            """
                $FACADE_IMPORT

                @Facade(FileSubsystem::class)
                class RemoteFileFacade {
                    fun find(): FileEntity = TODO()
                }
            """,
        )

        this
            .compileModule(listOf(facade), listOf(subsystemsModule.outputDirectory))
            .let { result ->
                this.assertCompilationFails(
                    result,
                    "RemoteFileFacade.kt:5: Facade RemoteFileFacade exposes FileEntity of @FileSubsystem",
                )
            }
    }

    @Test
    fun `a facade names one subsystem at least`() = this.assertCompilationFails(
        "@Facade\nclass EmptyFacade",
        2,
        "Facade EmptyFacade names no subsystem",
    )

    @Test
    fun `a facade under a type alias names one subsystem at least`() = this.assertCompilationFails(
        "typealias Entry = Facade\n@Entry\nclass EmptyFacade",
        3,
        "Facade EmptyFacade names no subsystem",
    )

    @Test
    fun `an unresolved subsystem is left to the compiler`() {
        val result = this.compile("@Facade(Unknown::class)\nclass UnknownFacade")

        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertTrue("Unknown" in result.messages, result.messages)
        assertFalse(PROCESSOR_FAILURE in result.messages, result.messages)
    }

    @Test
    fun `a facade names markers of subsystems only`() = this.assertCompilationFails(
        "annotation class Plain\n@Facade(Plain::class)\nclass PlainFacade",
        3,
        "Facade PlainFacade names @Plain, which is not marked with @Facade.Subsystem",
    )

    @Test
    fun `a facade goes on a class, not on an interface`() = this.assertCompilationFails(
        "@Facade(FileSubsystem::class)\ninterface FileOperations",
        2,
        "@Facade goes on a class, not on the interface FileOperations",
    )

    @Test
    fun `a facade goes on a class, not on an annotation`() = this.assertCompilationFails(
        "@Facade(FileSubsystem::class)\nannotation class FileOperation",
        2,
        "@Facade goes on a class, not on the annotation FileOperation",
    )

    @Test
    fun `a nested class carries the marker of one subsystem at most`() = this.assertCompilationFails(
        "class FileModule {\n@FileSubsystem\n@BillingSubsystem\nclass SharedClient\n}",
        4,
        "FileModule.SharedClient carries the markers of several subsystems: @FileSubsystem, @BillingSubsystem",
    )

    @Test
    fun `a marker under a type alias counts as the marker itself`() = this.assertCompilationFails(
        "@FileMarker\n@BillingSubsystem\nclass SharedClient",
        3,
        "SharedClient carries the markers of several subsystems: @FileSubsystem, @BillingSubsystem",
    )

    @Test
    fun `a repeated marker names one subsystem`() {
        val result = this.compile(
            """
                @Facade.Subsystem
                @Repeatable
                annotation class ArchiveSubsystem

                @ArchiveSubsystem
                @ArchiveSubsystem
                class ArchiveEntity
            """,
        )

        assertEquals(ExitCode.OK, result.exitCode, result.messages)
    }

    @Test
    fun `a facade carries no marker`() = this.assertCompilationFails(
        "@Facade(FileSubsystem::class)\n@FileSubsystem\nclass MarkedFacade",
        3,
        "Facade MarkedFacade carries @FileSubsystem: a facade stands outside its subsystems",
    )

    private fun assertCompilationFails(source: String, line: Int, message: String) = this
        .compile(source)
        .let { result -> this.assertCompilationFails(result, "$VIOLATION_FILE:$line: $message") }

    private fun assertCompilationFails(result: JvmCompilationResult, vararg errors: String) {
        assertEquals(ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)

        errors.forEach { error ->
            assertTrue(error in result.messages) { "No \"$error\" in ${result.messages}" }
        }
    }

    // The import takes the first line of the file, so the source starts at the second one.
    private fun compile(source: String) = SourceFile
        .kotlin(VIOLATION_FILE, "$FACADE_IMPORT\n$source")
        .let(::compileSources)

    private fun compileSources(vararg files: SourceFile) = this.compileModule(files.toList() + SUBSYSTEMS_FILE)

    private fun compileModule(files: List<SourceFile>, classpath: List<File> = emptyList()) = KotlinCompilation()
        .apply {
            sources = files
            inheritClassPath = true
            classpaths = classpath
            configureKsp { symbolProcessorProviders += FacadeSymbolProcessorProvider() }
        }
        .compile()

    private companion object {

        const val VIOLATION_FILE = "Violation.kt"

        const val FACADE_IMPORT = "import pro.vlprojects.semantic.pattern.structural.facade.Facade"

        // kctfork prints it once a processor reports an error or throws.
        const val PROCESSOR_FAILURE = "Error occurred in KSP"

        val SUBSYSTEMS_FILE = SourceFile.kotlin(
            "Subsystems.kt",
            """
                $FACADE_IMPORT

                @Facade.Subsystem
                annotation class FileSubsystem

                @Facade.Subsystem
                annotation class BillingSubsystem

                typealias FileMarker = FileSubsystem

                typealias PersistedFile = FileEntity

                @FileSubsystem
                class FileEntity

                @FileMarker
                class FileStorageClient

                @Facade(FileMarker::class)
                class FileFacade(private val storage: FileStorageClient, private val file: FileEntity)
            """,
        )
    }
}
