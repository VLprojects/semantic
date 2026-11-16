package pro.vlprojects.semantic.pattern.structural.facade

import kotlin.reflect.KClass

/**
 * An entry to the [subsystems] it names, each an annotation marked with [Subsystem]. Only a class
 * of a subsystem or a facade that names it takes a class of the subsystem, and a facade answers with types of its own:
 *
 * ```kotlin
 * @Facade.Subsystem
 * annotation class FileSubsystem
 *
 * @FileSubsystem
 * class FileStorageClient
 *
 * @Facade(FileSubsystem::class)
 * class FileFacade(private val storage: FileStorageClient)
 * ```
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
public annotation class Facade(vararg val subsystems: KClass<out Annotation>) {

    /** Marks the annotation that each class of one subsystem carries. */
    @MustBeDocumented
    @Target(AnnotationTarget.ANNOTATION_CLASS)
    public annotation class Subsystem
}
