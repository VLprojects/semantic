package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class InitializationFailingBackoff : Backoff {

    companion object {

        init {
            error("A failing backoff is initialized")
        }
    }
}
