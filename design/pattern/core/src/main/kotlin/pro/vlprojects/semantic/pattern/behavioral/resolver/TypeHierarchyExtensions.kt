package pro.vlprojects.semantic.pattern.behavioral.resolver

internal fun Class<*>.collectTypeHierarchy() = this
    .collectClassesBelowAny()
    .let { classes -> generateSequence(classes, List<Class<*>>::collectDirectInterfaces) }
    .takeWhile(List<Class<*>>::isNotEmpty)
    .flatten()
    .distinct()
    .plus(Any::class.java)
    .toList()

private fun Class<*>.collectClassesBelowAny() = generateSequence(this, Class<*>::getSuperclass)
    .takeWhile { type -> type != Any::class.java }
    .toList()

private fun List<Class<*>>.collectDirectInterfaces() = this
    .flatMap { type -> type.interfaces.asList() }
    .distinct()
