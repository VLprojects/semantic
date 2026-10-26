package pro.vlprojects.semantic.ddd.support

internal fun List<Any>.hasSameElementsAs(other: List<Any>) =
    size == other.size && groupingBy { it }.eachCount() == other.groupingBy { it }.eachCount()

internal fun List<Any>.unorderedHashCode() = sumOf { it.hashCode() }
