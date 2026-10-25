package pro.vlprojects.semantic.ddd.support

internal fun List<Any>.hasSameElementsAs(other: List<Any>): Boolean {
    if (size != other.size) return false
    val counts = groupingBy { it }.eachCountTo(HashMap())
    for (element in other) {
        when (val count = counts[element]) {
            null -> return false
            1 -> counts.remove(element)
            else -> counts[element] = count - 1
        }
    }
    return true
}

internal fun List<Any>.unorderedHashCode() = sumOf { it.hashCode() }
