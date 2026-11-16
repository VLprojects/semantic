package pro.vlprojects.semantic.pattern

/**
 * Marks the annotation of a pattern with its [purpose] and [scope], the two axes the Gang of Four classify patterns
 * by; [Purpose.ENTERPRISE] stands for the patterns of Fowler.
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class Pattern(val purpose: Purpose, val scope: Scope = Scope.OBJECT) {

    public enum class Purpose { CREATIONAL, STRUCTURAL, BEHAVIORAL, ENTERPRISE }

    public enum class Scope { CLASS, OBJECT }
}
