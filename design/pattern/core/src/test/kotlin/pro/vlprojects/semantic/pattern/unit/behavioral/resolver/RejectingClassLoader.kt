package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class RejectingClassLoader : ClassLoader(null) {

    override fun loadClass(name: String): Class<*> = error("The class '$name' is loaded")
}
