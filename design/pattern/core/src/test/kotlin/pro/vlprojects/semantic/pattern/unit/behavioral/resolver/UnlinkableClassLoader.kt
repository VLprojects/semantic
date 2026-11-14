package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class UnlinkableClassLoader : ClassLoader(null) {

    override fun loadClass(name: String): Class<*> = throw NoClassDefFoundError("The class '$name' is not linked")
}
