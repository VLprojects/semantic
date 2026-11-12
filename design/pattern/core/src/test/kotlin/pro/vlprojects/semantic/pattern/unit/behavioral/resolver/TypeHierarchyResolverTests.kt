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

    private val selectType = { type: KClass<*> -> type }

    private val types = mutableListOf(
        Any::class,
        Exception::class,
        RetryableFailure::class,
        IOException::class,
        TransientFailure::class,
        SocketTimeoutException::class,
    )

    private val resolver = TypeHierarchyResolver.of<Any, KClass<*>>(types, selectType)

    private val ioTypes = listOf(IOException::class)

    private val ioResolver = TypeHierarchyResolver.of<Exception, KClass<*>>(ioTypes, selectType)

    private val interfaceTypes = listOf(RetryableFailure::class, Serializable::class, TransientFailure::class)

    private val interfaceResolver = TypeHierarchyResolver.of<Throwable, KClass<*>>(interfaceTypes, selectType)

    private val first = ResolvingPolicy.first<KClass<*>>()

    private val connectTimeout = ConnectTimeoutException()

    private val ioFailure = IOException()

    private val ioPolicy = RetryPolicy(IOException::class)

    @Test
    fun `the candidate of the type of a context is resolved`() {
        val resolved = ioResolver.resolve(ioFailure)

        assertEquals(IOException::class, resolved)
    }

    @Test
    fun `the candidates are resolved from the nearest superclass to the farthest`() {
        val resolved = SocketTimeoutException().let(resolver::resolveAll)

        assertEquals(listOf(SocketTimeoutException::class, IOException::class, Exception::class, Any::class), resolved)
    }

    @Test
    fun `the interfaces follow the superclasses and precede Any`() {
        val resolved = resolver.resolveAll(connectTimeout)

        assertEquals(
            listOf(
                SocketTimeoutException::class,
                IOException::class,
                Exception::class,
                TransientFailure::class,
                RetryableFailure::class,
                Any::class,
            ),
            resolved,
        )
    }

    @Test
    fun `the interfaces are resolved level by level`() {
        val resolved = interfaceResolver.resolveAll(connectTimeout)

        assertEquals(listOf(TransientFailure::class, Serializable::class, RetryableFailure::class), resolved)
    }

    @Test
    fun `an interface reached twice is resolved once`() {
        val resolved = ReadTimeoutException().let(interfaceResolver::resolveAll)

        assertEquals(listOf(TransientFailure::class, RetryableFailure::class, Serializable::class), resolved)
    }

    @Test
    fun `the candidate of Any is resolved for a context of Any`() {
        val resolved = Any().let(resolver::resolveAll)

        assertEquals(listOf(Any::class), resolved)
    }

    @Test
    fun `the class Enum of an enum precedes its interfaces as any superclass`() {
        val enumTypes = listOf(TemporalAccessor::class, Enum::class)
        val enumResolver = TypeHierarchyResolver.of<Any, KClass<*>>(enumTypes, selectType)

        val resolved = enumResolver.resolveAll(DayOfWeek.MONDAY)

        assertEquals(listOf(Enum::class, TemporalAccessor::class), resolved)
    }

    @Test
    fun `the candidate of an enum is resolved for its constant with a body`() {
        val signalTypes = listOf(Signal::class)
        val signalResolver = TypeHierarchyResolver.of<Any, KClass<*>>(signalTypes, selectType)

        val resolved = signalResolver.resolveAll(Signal.RED)

        assertEquals(listOf(Signal::class), resolved)
    }

    @Test
    fun `an array resolves its own class and the interfaces of every array`() {
        val arrayTypes = listOf(
            Array<String>::class,
            Array<CharSequence>::class,
            Array<out Any>::class,
            Cloneable::class,
            Serializable::class,
        )
        val arrayResolver = TypeHierarchyResolver.of<Any, KClass<*>>(arrayTypes, selectType)

        val resolved = arrayOf("x").let(arrayResolver::resolveAll)

        assertEquals(listOf(Array<String>::class, Cloneable::class, Serializable::class), resolved)
    }

    @Test
    fun `the candidate of a Kotlin primitive is resolved for its boxed context`() {
        val numberTypes = listOf(Int::class, Number::class)
        val numberResolver = TypeHierarchyResolver.of<Any, KClass<*>>(numberTypes, selectType)

        val resolved = numberResolver.resolveAll(5)

        assertEquals(listOf(Int::class, Number::class), resolved)
    }

    @Test
    fun `the candidate of a Kotlin mapped interface is resolved for its Java context`() {
        val textTypes = listOf(CharSequence::class)
        val textResolver = TypeHierarchyResolver.of<Any, KClass<*>>(textTypes, selectType)

        val resolved = textResolver.resolveAll("x")

        assertEquals(listOf(CharSequence::class), resolved)
    }

    @Test
    fun `the candidate of a subtype is not resolved for its supertype`() {
        val resolved = Exception().let(ioResolver::resolveOrNull)

        assertNull(resolved)
    }

    @Test
    fun `several types of a context are not resolved without a policy`() {
        assertThrows<MultipleResolvedException> { resolver.resolve(connectTimeout) }
    }

    @Test
    fun `the nearest type is selected by the first policy`() {
        val resolved = resolver.resolve(connectTimeout, first)

        assertEquals(SocketTimeoutException::class, resolved)
    }

    @Test
    fun `a candidate is resolved by the type it declares`() {
        val exceptionPolicy = RetryPolicy(Exception::class)
        val policies = listOf(exceptionPolicy, ioPolicy)
        val policyResolver = TypeHierarchyResolver.of<Throwable, RetryPolicy>(policies, RetryPolicy::failureType)

        val resolved = policyResolver.resolveAll(connectTimeout)

        assertEquals(listOf(ioPolicy, exceptionPolicy), resolved)
    }

    @Test
    fun `the candidates of one type are resolved in their order`() {
        val fallbackIoPolicy = RetryPolicy(IOException::class)
        val policies = listOf(ioPolicy, fallbackIoPolicy)
        val policyResolver = TypeHierarchyResolver.of<Throwable, RetryPolicy>(policies, RetryPolicy::failureType)

        val resolved = policyResolver.resolveAll(ioFailure)

        assertEquals(listOf(ioPolicy, fallbackIoPolicy), resolved)
    }

    @Test
    fun `no candidates resolve nothing`() {
        val noTypes = emptyList<KClass<*>>()
        val emptyResolver = TypeHierarchyResolver.of<Any, KClass<*>>(noTypes, selectType)

        val resolved = emptyResolver.resolveAll(ioFailure)

        assertEquals(emptyList<KClass<*>>(), resolved)
    }

    @Test
    fun `a change of the source candidates does not reach the resolver`() {
        types.clear()

        val resolved = resolver.resolve(ioFailure, first)

        assertEquals(IOException::class, resolved)
    }
}
