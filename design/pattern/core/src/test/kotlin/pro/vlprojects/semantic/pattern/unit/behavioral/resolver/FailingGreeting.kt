package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class FailingGreeting : Greeting {

    companion object {

        init {
            error("A failing greeting is initialized")
        }
    }
}
