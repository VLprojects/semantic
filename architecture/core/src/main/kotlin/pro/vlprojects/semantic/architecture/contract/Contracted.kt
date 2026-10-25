package pro.vlprojects.semantic.architecture.contract

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
public annotation class Contracted(
    val namespace: String,
    val name: String,
    val scope: IntegrationScope,
    val version: Int = 1,
    val breaking: Boolean = false,
)
