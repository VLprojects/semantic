package pro.vlprojects.semantic.pattern.unit.creational.registry

class BrokenConnection(
    private val failure: Exception,
) : Connection() {

    override fun close() {
        super.close()
        throw failure
    }
}
