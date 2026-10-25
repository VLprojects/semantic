package pro.vlprojects.semantic.architecture.integration

@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
public annotation class Since(
    val version: Int,
)
