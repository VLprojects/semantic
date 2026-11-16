package pro.vlprojects.semantic.pattern.structural.facade

import kotlin.reflect.KClass

/**
 * An entry to a subsystem. [subsystem] is the opt-in marker that each class of the subsystem
 * carries, so a client compiles against a facade alone:
 *
 * ```kotlin
 * @RequiresOptIn(level = RequiresOptIn.Level.ERROR)
 * @Retention(AnnotationRetention.RUNTIME)
 * @Target(AnnotationTarget.CLASS)
 * annotation class FileSubsystem
 *
 * @FileSubsystem
 * class FileStorageClient
 *
 * @Facade(FileSubsystem::class)
 * @OptIn(FileSubsystem::class)
 * class FileFacade(private val storage: FileStorageClient)
 * ```
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
public annotation class Facade(val subsystem: KClass<out Annotation>)
