package pro.vlprojects.semantic.boundary

@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
public annotation class IntroducedIn(
    val version: Int,
)
