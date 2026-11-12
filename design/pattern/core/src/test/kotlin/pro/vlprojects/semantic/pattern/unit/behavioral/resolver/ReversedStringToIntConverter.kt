package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class ReversedStringToIntConverter : ReversedConverter<Int, String>() {

    override fun convert(source: String) = source.toInt()
}
