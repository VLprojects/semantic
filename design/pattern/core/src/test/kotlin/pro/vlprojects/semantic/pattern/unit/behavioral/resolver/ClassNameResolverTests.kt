package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.ClassNameResolver
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.ClassNotAllowedException
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.NotSubtypeException
import java.net.URLClassLoader
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.reflect.KClass

class ClassNameResolverTests {

    private val trustedPackage = Greeting::class.java.packageName

    private val resolver = ClassNameResolver.of(Greeting::class, setOf(trustedPackage))

    @Test
    fun `a subtype is resolved by the full name of its class`() {
        assertEquals(Hello::class, resolver.resolve(Hello::class.java.name))
    }

    @Test
    fun `a class of any of the trusted packages is resolved`() {
        val acmeResolver = ClassNameResolver.of(Greeting::class, listOf("com.acme", trustedPackage))

        assertEquals(Hello::class, acmeResolver.resolve(Hello::class.java.name))
    }

    @Test
    fun `the base type is resolved by its own name`() {
        assertEquals(Greeting::class, resolver.resolve(Greeting::class.java.name))
    }

    @Test
    fun `a subtype is resolved without initializing its class`() {
        assertEquals(FailingGreeting::class, resolver.resolve(FailingGreeting::class.java.name))
    }

    @Test
    fun `a class of a subpackage is trusted`() {
        val parentPackage = trustedPackage.substringBeforeLast('.')
        val parentResolver = ClassNameResolver.of(Greeting::class, setOf(parentPackage))

        assertEquals(Hello::class, parentResolver.resolve(Hello::class.java.name))
    }

    @Test
    fun `a missing class in a trusted package resolves nothing`() {
        assertNull(resolver.resolveOrNull("$trustedPackage.Goodbye"))
    }

    @Test
    fun `a class that the class loader does not see resolves nothing`() {
        val platformResolver = ClassNameResolver.of(
            Greeting::class,
            setOf(trustedPackage),
            ClassLoader.getPlatformClassLoader(),
        )

        assertNull(platformResolver.resolveOrNull(Hello::class.java.name))
    }

    @Test
    fun `a class outside the trusted packages is rejected`() {
        val failure = assertThrows<ClassNotAllowedException> { resolver.resolveOrNull("java.lang.Runtime") }

        assertEquals("java.lang.Runtime", failure.className)
        assertEquals(setOf(trustedPackage), failure.trustedPackages)
        assertEquals(
            "The class 'java.lang.Runtime' is not in the trusted packages: [$trustedPackage]",
            failure.message,
        )
    }

    @Test
    fun `a trusted package matches whole names of packages only`() {
        val truncatedPackage = trustedPackage.dropLast(1)
        val prefixResolver = ClassNameResolver.of(Greeting::class, setOf(truncatedPackage))

        assertThrows<ClassNotAllowedException> { prefixResolver.resolveOrNull(Hello::class.java.name) }
    }

    @Test
    fun `the name of a trusted package itself is rejected`() {
        assertThrows<ClassNotAllowedException> { resolver.resolveOrNull(trustedPackage) }
    }

    @Test
    fun `a change of the packages given to a failure does not reach it`() {
        val packages = mutableSetOf("com.acme")
        val failure = ClassNotAllowedException("java.lang.Runtime", packages)

        packages += "java.lang"

        assertEquals(setOf("com.acme"), failure.trustedPackages)
    }

    @Test
    fun `a class outside the trusted packages is rejected before it is loaded`() {
        val rejectingResolver = ClassNameResolver.of(Greeting::class, setOf("com.acme"), RejectingClassLoader())

        assertThrows<ClassNotAllowedException> { rejectingResolver.resolveOrNull(Hello::class.java.name) }
    }

    @Test
    fun `the trusted packages of a failure cannot be changed`() {
        val acmeResolver = ClassNameResolver.of(Greeting::class, setOf("com.acme", trustedPackage))
        val failure = assertThrows<ClassNotAllowedException> { acmeResolver.resolveOrNull("java.lang.Runtime") }

        assertThrows<UnsupportedOperationException> { (failure.trustedPackages as MutableSet<String>).add("java.lang") }
    }

    @Test
    fun `a class that cannot be linked is rejected`() {
        val unlinkableResolver = ClassNameResolver.of(Greeting::class, setOf(trustedPackage), UnlinkableClassLoader())

        val failure = assertThrows<IllegalArgumentException> {
            unlinkableResolver.resolveOrNull(Hello::class.java.name)
        }

        assertEquals("The class '${Hello::class.java.name}' cannot be loaded", failure.message)
        assertInstanceOf(NoClassDefFoundError::class.java, failure.cause)
    }

    @Test
    fun `a change of the source packages does not reach the resolver`() {
        val packages = mutableListOf("com.acme")
        val acmeResolver = ClassNameResolver.of(Greeting::class, packages)

        packages += trustedPackage

        assertThrows<ClassNotAllowedException> { acmeResolver.resolveOrNull(Hello::class.java.name) }
    }

    @Test
    fun `the context class loader of the thread goes before the one of the base type`() {
        val platformResolver = createResolverInThread(ClassLoader.getPlatformClassLoader(), Greeting::class)

        assertNull(platformResolver.resolveOrNull(Hello::class.java.name))
    }

    @Test
    fun `the class loader of the base type is taken when the thread has no context class loader`() {
        val testClassesLocation = Greeting::class.java.protectionDomain.codeSource.location

        URLClassLoader(arrayOf(testClassesLocation), null).use { isolatedClassLoader ->
            val isolatedGreeting = isolatedClassLoader
                .loadClass(Greeting::class.java.name)
                .kotlin

            val isolatedHello = createResolverInThread(threadClassLoader = null, isolatedGreeting)
                .resolve(Hello::class.java.name)

            assertSame(isolatedClassLoader, isolatedHello.java.classLoader)
        }
    }

    @Test
    fun `the system class loader is taken when neither the thread nor the base type has one`() {
        val anyResolver = createResolverInThread(threadClassLoader = null, Any::class)

        assertEquals(Hello::class, anyResolver.resolve(Hello::class.java.name))
    }

    @Test
    fun `a nested class is resolved by its binary name`() {
        assertEquals(Greetings.Morning::class, resolver.resolve(Greetings.Morning::class.java.name))
    }

    @Test
    fun `an array of a trusted class is rejected`() {
        assertThrows<ClassNotAllowedException> { resolver.resolveOrNull("[L${Hello::class.java.name};") }
    }

    @Test
    fun `a class that is not a subtype is rejected`() {
        val failure = assertThrows<NotSubtypeException> { resolver.resolveOrNull(Farewell::class.java.name) }

        assertEquals(Farewell::class.java.name, failure.className)
        assertEquals(Greeting::class, failure.baseType)
        assertEquals(
            "The class '${Farewell::class.java.name}' is not a subtype of ${Greeting::class.java.name}",
            failure.message,
        )
    }

    @Test
    fun `empty trusted packages are rejected`() {
        val failure = assertThrows<IllegalArgumentException> { ClassNameResolver.of(Greeting::class, emptySet()) }

        assertEquals("Trusted packages are empty", failure.message)
    }

    @Test
    fun `trusted packages that are not package names are rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            ClassNameResolver.of(Greeting::class, listOf("com.acme", "com.acme.", "*", "", "com..acme", "1com"))
        }

        assertEquals(
            "Trusted packages are not package names: ['com.acme.', '*', '', 'com..acme', '1com']",
            failure.message,
        )
    }

    private fun <T : Any> createResolverInThread(threadClassLoader: ClassLoader?, baseType: KClass<T>) = Executors
        .newSingleThreadExecutor { task ->
            Thread(task).apply { contextClassLoader = threadClassLoader }
        }
        .use { executor ->
            executor
                .submit(
                    Callable {
                        ClassNameResolver.of(baseType, setOf(trustedPackage))
                    },
                )
                .get()
        }
}
