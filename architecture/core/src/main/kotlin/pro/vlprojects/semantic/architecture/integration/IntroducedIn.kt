package pro.vlprojects.semantic.architecture.integration

@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
public annotation class IntroducedIn(
    val version: Int,
)
