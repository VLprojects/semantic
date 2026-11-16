package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

fun interface Converter<S, T> {

    fun convert(source: S): T
}
