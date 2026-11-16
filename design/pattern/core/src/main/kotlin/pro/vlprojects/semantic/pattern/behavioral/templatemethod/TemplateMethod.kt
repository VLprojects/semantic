package pro.vlprojects.semantic.pattern.behavioral.templatemethod

import pro.vlprojects.semantic.pattern.Pattern

/**
 * The skeleton of an algorithm that defers its steps to subclasses: a final function of an [AbstractClass] that calls
 * its [PrimitiveOperation] and [HookOperation] steps. A step names in `templateMethods` the skeletons it serves,
 * which may be left out when its class has one skeleton:
 *
 * ```kotlin
 * @TemplateMethod.AbstractClass
 * abstract class Application {
 *
 *     @TemplateMethod
 *     fun openDocument(name: String) {
 *         if (!this.canOpenDocument(name)) return
 *
 *         this.doCreateDocument().read(name)
 *     }
 *
 *     @TemplateMethod.PrimitiveOperation("openDocument")
 *     protected abstract fun doCreateDocument(): Document
 *
 *     @TemplateMethod.HookOperation("openDocument")
 *     protected open fun canOpenDocument(name: String) = true
 * }
 * ```
 */
@Pattern(Pattern.Purpose.BEHAVIORAL, Pattern.Scope.CLASS)
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
public annotation class TemplateMethod {

    /** Marks the abstract class that declares template methods and their steps. */
    @MustBeDocumented
    @Target(AnnotationTarget.CLASS)
    public annotation class AbstractClass

    /** Marks a class that implements the steps of an [AbstractClass] it extends. */
    @MustBeDocumented
    @Target(AnnotationTarget.CLASS)
    public annotation class ConcreteClass

    /** Marks an abstract step that each [ConcreteClass] implements. */
    @MustBeDocumented
    @Target(AnnotationTarget.FUNCTION)
    public annotation class PrimitiveOperation(vararg val templateMethods: String)

    /** Marks an open step with a default, often empty, that a [ConcreteClass] may override. */
    @MustBeDocumented
    @Target(AnnotationTarget.FUNCTION)
    public annotation class HookOperation(vararg val templateMethods: String)
}
