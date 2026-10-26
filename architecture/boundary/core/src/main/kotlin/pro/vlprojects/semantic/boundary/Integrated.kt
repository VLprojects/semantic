package pro.vlprojects.semantic.boundary

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
public annotation class Integrated(
    val namespace: String,
    val name: String,
    val scope: Scope,
    val version: Int = 1,
    val breaksBackwardCompatibility: Boolean = false,
)
