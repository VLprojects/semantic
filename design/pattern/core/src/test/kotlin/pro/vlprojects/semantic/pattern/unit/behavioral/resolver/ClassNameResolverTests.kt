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

    private val trustedPackage = Backoff::class.java.packageName

    private val resolver = ClassNameResolver.of(Backoff::class, setOf(trustedPackage))

    @Test
    fun `a subtype is resolved by the full name of its class`() {
        assertEquals(FixedBackoff::class, resolver.resolve(FixedBackoff::class.java.name))
    }

    @Test
    fun `the base type is resolved by its own name`() {
        assertEquals(Backoff::class, resolver.resolve(Backoff::class.java.name))
    }

    @Test
    fun `a nested class is resolved by its binary name`() {
        assertEquals(Backoffs.Jittered::class, resolver.resolve(Backoffs.Jittered::class.java.name))
    }

    @Test
    fun `a missing class in a trusted package resolves nothing`() {
        assertNull(resolver.resolveOrNull("$trustedPackage.MissingBackoff"))
    }

    @Test
    fun `a class of any of the trusted packages is resolved`() {
        val acmeResolver = ClassNameResolver.of(Backoff::class, listOf("com.acme", trustedPackage))

        assertEquals(FixedBackoff::class, acmeResolver.resolve(FixedBackoff::class.java.name))
    }

    @Test
    fun `a class of a subpackage is trusted`() {
        val parentPackage = trustedPackage.substringBeforeLast('.')
        val parentResolver = ClassNameResolver.of(Backoff::class, setOf(parentPackage))

        assertEquals(FixedBackoff::class, parentResolver.resolve(FixedBackoff::class.java.name))
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
    fun `a class outside the trusted packages is rejected before it is loaded`() {
        val rejectingResolver = ClassNameResolver.of(Backoff::class, setOf("com.acme"), RejectingClassLoader())

        assertThrows<ClassNotAllowedException> { rejectingResolver.resolveOrNull(FixedBackoff::class.java.name) }
    }

    @Test
    fun `a name that only starts as a trusted package is rejected`() {
        val truncatedPackage = trustedPackage.dropLast(1)
        val truncatedResolver = ClassNameResolver.of(Backoff::class, setOf(truncatedPackage))

        assertThrows<ClassNotAllowedException> { truncatedResolver.resolveOrNull(FixedBackoff::class.java.name) }
        assertThrows<ClassNotAllowedException> { resolver.resolveOrNull(trustedPackage) }
        assertThrows<ClassNotAllowedException> { resolver.resolveOrNull("[L${FixedBackoff::class.java.name};") }
    }

    @Test
    fun `a change of the source packages does not reach the resolver`() {
        val packages = mutableListOf("com.acme")
        val acmeResolver = ClassNameResolver.of(Backoff::class, packages)

        packages += trustedPackage

        assertThrows<ClassNotAllowedException> { acmeResolver.resolveOrNull(FixedBackoff::class.java.name) }
    }

    @Test
    fun `a subtype is resolved without initializing its class`() {
        assertEquals(FailingBackoff::class, resolver.resolve(FailingBackoff::class.java.name))
    }

    @Test
    fun `a class that cannot be linked is rejected`() {
        val unlinkableResolver = ClassNameResolver.of(Backoff::class, setOf(trustedPackage), UnlinkableClassLoader())

        val failure = assertThrows<IllegalArgumentException> {
            unlinkableResolver.resolveOrNull(FixedBackoff::class.java.name)
        }

        assertEquals("The class '${FixedBackoff::class.java.name}' cannot be loaded", failure.message)
        assertInstanceOf(NoClassDefFoundError::class.java, failure.cause)
    }

    @Test
    fun `the context class loader of the thread goes before the one of the base type`() {
        val platformResolver = ClassLoader.getPlatformClassLoader()
            .let { platformClassLoader -> createResolverInThread(platformClassLoader, Backoff::class) }

        assertNull(platformResolver.resolveOrNull(FixedBackoff::class.java.name))
    }

    @Test
    fun `the class loader of the base type is taken when the thread has no context class loader`() {
        val testClassesLocation = Backoff::class.java.protectionDomain.codeSource.location

        URLClassLoader(arrayOf(testClassesLocation), null).use { isolatedClassLoader ->
            val isolatedBackoff = isolatedClassLoader
                .loadClass(Backoff::class.java.name)
                .kotlin

            val isolatedFixedBackoff = createResolverInThread(threadClassLoader = null, isolatedBackoff)
                .resolve(FixedBackoff::class.java.name)

            assertSame(isolatedClassLoader, isolatedFixedBackoff.java.classLoader)
        }
    }

    @Test
    fun `the system class loader is taken when neither the thread nor the base type has one`() {
        val anyResolver = createResolverInThread(threadClassLoader = null, Any::class)

        assertEquals(FixedBackoff::class, anyResolver.resolve(FixedBackoff::class.java.name))
    }

    @Test
    fun `a class that is not a subtype is rejected`() {
        val testsName = ClassNameResolverTests::class.java.name
        val failure = assertThrows<NotSubtypeException> { resolver.resolveOrNull(testsName) }

        assertEquals(testsName, failure.className)
        assertEquals(Backoff::class, failure.baseType)
        assertEquals("The class '$testsName' is not a subtype of ${Backoff::class.java.name}", failure.message)
    }

    @Test
    fun `empty trusted packages are rejected`() {
        val failure = assertThrows<IllegalArgumentException> { ClassNameResolver.of(Backoff::class, emptySet()) }

        assertEquals("Trusted packages are empty", failure.message)
    }

    @Test
    fun `trusted packages that are not package names are rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            ClassNameResolver.of(
                Backoff::class,
                listOf("com.acme", "com.acme.", "*", "", "com..acme", "1com", "com.ac-me"),
            )
        }

        assertEquals(
            "Trusted packages are not package names: ['com.acme.', '*', '', 'com..acme', '1com', 'com.ac-me']",
            failure.message,
        )
    }

    @Test
    fun `the trusted packages of a failure are its own unmodifiable copy`() {
        val packages = mutableSetOf("com.acme", "org.acme")
        val failure = ClassNotAllowedException("java.lang.Runtime", packages)

        packages += "java.lang"

        assertEquals(setOf("com.acme", "org.acme"), failure.trustedPackages)
        assertThrows<UnsupportedOperationException> { (failure.trustedPackages as MutableSet<String>).add("java.lang") }
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
