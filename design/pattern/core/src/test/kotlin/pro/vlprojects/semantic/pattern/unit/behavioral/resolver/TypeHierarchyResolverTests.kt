package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.ResolvingPolicy
import pro.vlprojects.semantic.pattern.behavioral.resolver.TypeHierarchyResolver
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.MultipleResolvedException
import java.io.IOException
import java.io.Serializable
import java.net.SocketTimeoutException
import java.time.DayOfWeek
import java.time.temporal.TemporalAccessor
import kotlin.reflect.KClass

class TypeHierarchyResolverTests {

    private val labels = mutableMapOf(
        Any::class to "any",
        Exception::class to "exception",
        RetryableFailure::class to "retryable",
        IOException::class to "io",
        TransientFailure::class to "transient",
        SocketTimeoutException::class to "socket timeout",
    )

    private val resolver = TypeHierarchyResolver.of<Any, String>(labels)

    private val ioLabels = mapOf(IOException::class to "io")

    private val ioResolver = TypeHierarchyResolver.of<Exception, String>(ioLabels)

    private val interfaceLabels = mapOf(
        RetryableFailure::class to "retryable",
        Serializable::class to "serializable",
        TransientFailure::class to "transient",
    )

    private val interfaceResolver = TypeHierarchyResolver.of<Throwable, String>(interfaceLabels)

    private val first = ResolvingPolicy.first<String>()

    private val connectTimeout = ConnectTimeoutException()

    private val ioFailure = IOException()

    @Test
    fun `the value of the type of a context is resolved`() {
        val resolved = ioResolver.resolve(ioFailure)

        assertEquals("io", resolved)
    }

    @Test
    fun `the values are resolved from the nearest superclass to the farthest`() {
        val resolved = SocketTimeoutException().let(resolver::resolveAll)

        assertEquals(listOf("socket timeout", "io", "exception", "any"), resolved)
    }

    @Test
    fun `the interfaces follow the superclasses and precede Any`() {
        val resolved = resolver.resolveAll(connectTimeout)

        assertEquals(listOf("socket timeout", "io", "exception", "transient", "retryable", "any"), resolved)
    }

    @Test
    fun `the interfaces are resolved level by level`() {
        val resolved = interfaceResolver.resolveAll(connectTimeout)

        assertEquals(listOf("transient", "serializable", "retryable"), resolved)
    }

    @Test
    fun `an interface reached twice is resolved once`() {
        val resolved = ReadTimeoutException().let(interfaceResolver::resolveAll)

        assertEquals(listOf("transient", "retryable", "serializable"), resolved)
    }

    @Test
    fun `the value of Any is resolved for a context of Any`() {
        val resolved = Any().let(resolver::resolveAll)

        assertEquals(listOf("any"), resolved)
    }

    @Test
    fun `the class Enum of an enum precedes its interfaces as any superclass`() {
        val enumLabels = mapOf(TemporalAccessor::class to "temporal accessor", Enum::class to "enum")
        val enumResolver = TypeHierarchyResolver.of<Any, String>(enumLabels)

        val resolved = enumResolver.resolveAll(DayOfWeek.MONDAY)

        assertEquals(listOf("enum", "temporal accessor"), resolved)
    }

    @Test
    fun `the value of an enum is resolved for its constant with a body`() {
        val signalLabels = mapOf(Signal::class to "signal")
        val signalResolver = TypeHierarchyResolver.of<Any, String>(signalLabels)

        val resolved = signalResolver.resolveAll(Signal.RED)

        assertEquals(listOf("signal"), resolved)
    }

    @Test
    fun `an array resolves its own class and the interfaces of every array`() {
        val arrayLabels = mapOf(
            Array<String>::class to "strings",
            Array<CharSequence>::class to "char sequences",
            Array<out Any>::class to "objects",
            Cloneable::class to "cloneable",
            Serializable::class to "serializable",
        )
        val arrayResolver = TypeHierarchyResolver.of<Any, String>(arrayLabels)

        val resolved = arrayOf("x").let(arrayResolver::resolveAll)

        assertEquals(listOf("strings", "cloneable", "serializable"), resolved)
    }

    @Test
    fun `the value of a Kotlin primitive is resolved for its boxed context`() {
        val numberLabels = mapOf(Int::class to "int", Number::class to "number")
        val numberResolver = TypeHierarchyResolver.of<Any, String>(numberLabels)

        val resolved = numberResolver.resolveAll(5)

        assertEquals(listOf("int", "number"), resolved)
    }

    @Test
    fun `the value of a Kotlin mapped interface is resolved for its Java context`() {
        val textLabels = mapOf(CharSequence::class to "char sequence")
        val textResolver = TypeHierarchyResolver.of<Any, String>(textLabels)

        val resolved = textResolver.resolveAll("x")

        assertEquals(listOf("char sequence"), resolved)
    }

    @Test
    fun `the value of a subtype is not resolved for its supertype`() {
        val resolved = Exception().let(ioResolver::resolveOrNull)

        assertNull(resolved)
    }

    @Test
    fun `several types of a context are not resolved without a policy`() {
        assertThrows<MultipleResolvedException> { resolver.resolve(connectTimeout) }
    }

    @Test
    fun `the nearest type is selected by the first policy`() {
        assertEquals("socket timeout", resolver.resolve(connectTimeout, first))
    }

    @Test
    fun `an empty map resolves nothing`() {
        val emptyLabels = emptyMap<KClass<*>, String>()
        val emptyResolver = TypeHierarchyResolver.of<Any, String>(emptyLabels)

        val resolved = emptyResolver.resolveAll(ioFailure)

        assertEquals(emptyList<String>(), resolved)
    }

    @Test
    fun `a change of the source map does not reach the resolver`() {
        labels.clear()

        assertEquals("io", resolver.resolve(ioFailure, first))
    }
}
