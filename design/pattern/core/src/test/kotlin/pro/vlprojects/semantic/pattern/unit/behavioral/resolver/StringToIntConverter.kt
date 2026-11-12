package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class StringToIntConverter : StringConverter<Int>() {

    override fun convert(source: String) = source.toInt()
}
