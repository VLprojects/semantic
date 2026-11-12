package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.ClassNameResolver
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.ClassNotAllowedException
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.NotSubtypeException
import pro.vlprojects.semantic.pattern.behavioral.resolver.getDefaultClassLoader
import java.net.URL
import java.net.URLClassLoader
import java.sql.Wrapper

class ClassNameResolverTests {

    private val allowedPackage = Backoff::class.java.packageName

    private val fixedBackoffName = FixedBackoff::class.java.name

    private val resolver = ClassNameResolver.of(Backoff::class, setOf(allowedPackage))

    @Test
    fun `a subtype is resolved by the full name of its class`() {
        assertEquals(FixedBackoff::class, resolver.resolve(fixedBackoffName))
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
    fun `a nested class is not resolved by its canonical name`() {
        assertNull(resolver.resolveOrNull(Backoffs.Jittered::class.java.canonicalName))
    }

    @Test
    fun `a missing class in an allowed package resolves nothing`() {
        assertNull(resolver.resolveOrNull("$allowedPackage.MissingBackoff"))
    }

    @Test
    fun `a malformed name in an allowed package resolves nothing`() {
        assertNull(resolver.resolveOrNull("$allowedPackage..FixedBackoff"))
    }

    @Test
    fun `a subtype of Any is resolved by the full name of its class`() {
        val anyResolver = ClassNameResolver.of(Any::class, setOf(allowedPackage))

        assertEquals(FixedBackoff::class, anyResolver.resolve(fixedBackoffName))
    }

    @Test
    fun `a primitive base type resolves its wrapper class`() {
        val intResolver = ClassNameResolver.of(Int::class, setOf("java.lang"))

        assertEquals(Int::class, intResolver.resolve(Int::class.javaObjectType.name))
    }

    @Test
    fun `a class of any of the allowed packages is resolved`() {
        val severalPackagesResolver = ClassNameResolver.of(Backoff::class, listOf("com.acme", allowedPackage))

        assertEquals(FixedBackoff::class, severalPackagesResolver.resolve(fixedBackoffName))
    }

    @Test
    fun `a class of a subpackage is allowed`() {
        val parentPackage = allowedPackage.substringBeforeLast('.')
        val parentResolver = ClassNameResolver.of(Backoff::class, setOf(parentPackage))

        assertEquals(FixedBackoff::class, parentResolver.resolve(fixedBackoffName))
    }

    @Test
    fun `a class outside the allowed packages is rejected`() {
        val failure = assertThrows<ClassNotAllowedException> { resolver.resolveAll("java.lang.Runtime") }

        assertEquals("java.lang.Runtime", failure.className)
        assertEquals(setOf(allowedPackage), failure.allowedPackages)
        assertEquals(
            "The class 'java.lang.Runtime' is not in the allowed packages: [$allowedPackage]",
            failure.message,
        )
    }

    @Test
    fun `a class outside the allowed packages is rejected before it is loaded`() {
        val rejectingResolver = ClassNameResolver.of(Backoff::class, setOf("com.acme"), RejectingClassLoader())

        assertThrows<ClassNotAllowedException> { rejectingResolver.resolveOrNull(fixedBackoffName) }
    }

    @Test
    fun `a package that only starts as an allowed one is not allowed`() {
        val truncatedPackage = allowedPackage.dropLast(1)
        val truncatedResolver = ClassNameResolver.of(Backoff::class, setOf(truncatedPackage))

        assertThrows<ClassNotAllowedException> { truncatedResolver.resolveOrNull(fixedBackoffName) }
    }

    @Test
    fun `the name of an allowed package is rejected`() {
        assertThrows<ClassNotAllowedException> { resolver.resolveOrNull(allowedPackage) }
    }

    @Test
    fun `the descriptor of an array of an allowed class is rejected`() {
        assertThrows<ClassNotAllowedException> { resolver.resolveOrNull("[L$fixedBackoffName;") }
    }

    @Test
    fun `a change of the source packages does not reach the resolver`() {
        val packages = mutableListOf("com.acme")
        val acmeResolver = ClassNameResolver.of(Backoff::class, packages)

        packages += allowedPackage

        assertThrows<ClassNotAllowedException> { acmeResolver.resolveOrNull(fixedBackoffName) }
    }

    @Test
    fun `a subtype is resolved without initializing its class`() {
        assertEquals(FailingBackoff::class, resolver.resolve(FailingBackoff::class.java.name))
    }

    @Test
    fun `a class that cannot be loaded is rejected`() {
        val unlinkableResolver = ClassNameResolver.of(Backoff::class, setOf(allowedPackage), UnlinkableClassLoader())

        val failure = assertThrows<IllegalArgumentException> {
            unlinkableResolver.resolveOrNull(fixedBackoffName)
        }

        assertEquals("The class '$fixedBackoffName' cannot be loaded", failure.message)
        assertInstanceOf(NoClassDefFoundError::class.java, failure.cause)
    }

    @Test
    fun `the class loader of the base type is taken when the resolver does not see the base type`() {
        val testClassesLocation = Backoff::class.java.protectionDomain.codeSource.location

        arrayOf(testClassesLocation)
            .let { urls -> URLClassLoader(urls, null) }
            .use { isolatedClassLoader ->
                val isolatedBackoff = isolatedClassLoader
                    .loadClass(Backoff::class.java.name)
                    .kotlin

                val isolatedFixedBackoff = ClassNameResolver.of(isolatedBackoff, setOf(allowedPackage))
                    .resolve(fixedBackoffName)

                assertSame(isolatedClassLoader, isolatedFixedBackoff.java.classLoader)
            }
    }

    @Test
    fun `the class loader of the resolver is taken when it sees the base type`() {
        val platformClassLoader = ClassLoader.getPlatformClassLoader()

        emptyArray<URL>()
            .let { urls -> URLClassLoader(urls, platformClassLoader) }
            .use { resolverClassLoader ->
                assertSame(resolverClassLoader, Wrapper::class.getDefaultClassLoader(resolverClassLoader))
                assertSame(resolverClassLoader, Any::class.getDefaultClassLoader(resolverClassLoader))
            }
    }

    @Test
    fun `the class loader of the base type is taken when the resolver has none`() {
        assertSame(Backoff::class.java.classLoader, Backoff::class.getDefaultClassLoader(resolverClassLoader = null))
    }

    @Test
    fun `the system class loader is taken when neither the resolver nor the base type has one`() {
        assertSame(ClassLoader.getSystemClassLoader(), Any::class.getDefaultClassLoader(resolverClassLoader = null))
    }

    @Test
    fun `a class that is not a subtype is rejected`() {
        val testsName = ClassNameResolverTests::class.java.name

        val failure = assertThrows<NotSubtypeException> { resolver.resolveOrNull(testsName) }

        assertEquals(testsName, failure.className)
        assertEquals(Backoff::class.java.name, failure.baseTypeName)
        assertEquals("The class '$testsName' is not a subtype of '${Backoff::class.java.name}'", failure.message)
    }

    @Test
    fun `empty allowed packages are rejected`() {
        val failure = assertThrows<IllegalArgumentException> { ClassNameResolver.of(Backoff::class, emptySet()) }

        assertEquals("Allowed packages are empty", failure.message)
    }

    @Test
    fun `allowed packages may hold keywords of Java, dollars and letters beyond ASCII`() {
        val packages = listOf(
            "com.my_app",
            "com.acme2",
            "_acme",
            "com.\$acme",
            "com.\uD835\uDC65",
            "com.int",
            "com.acme.native",
        )

        assertDoesNotThrow { ClassNameResolver.of(Backoff::class, packages) }
    }

    @Test
    fun `allowed packages that are not package names are rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            ClassNameResolver.of(Backoff::class, listOf("com.acme.", "*", "", "com..acme", "1com", "com.ac-me"))
        }

        assertEquals(
            "Allowed packages are not package names: ['com.acme.', '*', '', 'com..acme', '1com', 'com.ac-me']",
            failure.message,
        )
    }
}
