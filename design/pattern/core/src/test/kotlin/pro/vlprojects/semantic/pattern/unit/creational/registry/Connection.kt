package pro.vlprojects.semantic.pattern.unit.creational.registry

open class Connection : AutoCloseable {

    var isClosed = false
        private set

    override fun close() {
        isClosed = true
    }
}
