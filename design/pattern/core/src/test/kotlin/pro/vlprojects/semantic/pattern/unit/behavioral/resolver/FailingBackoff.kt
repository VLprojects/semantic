package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class FailingBackoff : Backoff {

    companion object {

        init {
            error("A failing backoff is initialized")
        }
    }
}
