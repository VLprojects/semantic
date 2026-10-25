package pro.vlprojects.semantic.architecture.integration

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
public annotation class Integrated(
    val namespace: String,
    val name: String,
    val scope: Scope,
    val version: Int = 1,
    val breaksBackwardCompatibility: Boolean = false,
)
