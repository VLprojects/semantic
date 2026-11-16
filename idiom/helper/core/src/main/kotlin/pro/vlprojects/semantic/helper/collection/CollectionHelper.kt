package pro.vlprojects.semantic.helper.collection

// O(n) time and memory: one map of counts, filled by this collection and emptied by the other.
public fun <T> Collection<T>.hasSameElementsAs(other: Collection<T>): Boolean {
    if (this.size != other.size) return false
    val counts = this.groupingBy { it }.eachCountTo(HashMap())
    for (element in other) {
        when (val count = counts[element]) {
            null -> return false
            1 -> counts.remove(element)
            else -> counts[element] = count - 1
        }
    }
    return true
}

// O(n) time, O(1) memory; the sum does not depend on the order of the elements.
public fun <T> Iterable<T>.sumHashCodes(): Int = this.sumOf { it.hashCode() }
