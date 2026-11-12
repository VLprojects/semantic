package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.GenericTypeResolver
import java.lang.reflect.Proxy

class GenericTypeResolverTests {

    private val commandTypes = GenericTypeResolver.of(CommandHandler::class)

    @Test
    fun `a type argument given to a generic type is resolved`() {
        assertEquals(CreateOrder::class, commandTypes.resolve(CreateOrderHandler::class))
    }

    @Test
    fun `a type argument given to an abstract base is resolved through its type variable`() {
        assertEquals(CancelOrder::class, commandTypes.resolve(CancelOrderHandler::class))
    }

    @Test
    fun `a type argument of a class proxied by a subclass is resolved`() {
        assertEquals(CreateOrder::class, commandTypes.resolve(ProxiedCreateOrderHandler::class))
    }

    @Test
    fun `a type argument given to a generic interface is resolved through its type variable`() {
        assertEquals(ConfirmOrder::class, commandTypes.resolve(ConfirmOrderHandler::class))
    }

    @Test
    fun `an array of a type variable is resolved as an array of its argument`() {
        assertEquals(Array<CreateOrder>::class, commandTypes.resolve(CreateOrderArrayHandler::class))
    }

    @Test
    fun `a type argument given along two paths is resolved`() {
        assertEquals(CreateOrder::class, commandTypes.resolve(DualCreateOrderHandler::class))
    }

    @Test
    fun `a type argument given by the wrapper of a Kotlin primitive is resolved`() {
        val comparableTypes = GenericTypeResolver.of(Comparable::class)

        assertEquals(Int::class, comparableTypes.resolve(Int::class))
    }

    @Test
    fun `a type argument is resolved by the index of its type parameter`() {
        val sourceTypes = GenericTypeResolver.of(Converter::class)
        val targetTypes = GenericTypeResolver.of(Converter::class, index = 1)

        assertEquals(String::class, sourceTypes.resolve(StringToIntConverter::class))
        assertEquals(Int::class, targetTypes.resolve(StringToIntConverter::class))
    }

    @Test
    fun `a type variable is followed by its declaration, not by its position`() {
        val sourceTypes = GenericTypeResolver.of(Converter::class)
        val targetTypes = GenericTypeResolver.of(Converter::class, index = 1)

        assertEquals(String::class, sourceTypes.resolve(ReversedStringToIntConverter::class))
        assertEquals(Int::class, targetTypes.resolve(ReversedStringToIntConverter::class))
    }

    @Test
    fun `a parameterized type argument is resolved as its raw class`() {
        assertEquals(List::class, commandTypes.resolve(BatchCommandHandler::class))
    }

    @Test
    fun `a type argument given to the outer class of an inner supertype is resolved`() {
        assertEquals(CreateOrder::class, commandTypes.resolve(OrderCommandHandlers.CreateOrderHandler::class))
    }

    @Test
    fun `a type argument given to the outer class of an outer class is resolved`() {
        val handlerType = OrderCommandModule.CreateOrderHandlers.CreateOrderHandler::class

        assertEquals(CreateOrder::class, commandTypes.resolve(handlerType))
    }

    @Test
    fun `a type argument given to the outer class wins over the variable an inner subclass passes back`() {
        val handlerType = CreateOrderOuterCommandHandler.CreateOrderInnerCommandHandler::class

        assertEquals(CreateOrder::class, commandTypes.resolve(handlerType))
    }

    @Test
    fun `a type argument given by an anonymous class is resolved`() {
        val handler = object : CommandHandler<CreateOrder> {

            override fun handle(command: CreateOrder) = Unit
        }

        assertEquals(CreateOrder::class, commandTypes.resolve(handler::class))
    }

    @Test
    fun `a type argument of a lambda is erased and resolves nothing`() {
        val handler = CommandHandler<CreateOrder> { _ -> }

        assertNull(commandTypes.resolveOrNull(handler::class))
    }

    @Test
    fun `a dynamic proxy implements the raw generic type and resolves nothing`() {
        val interfaces = arrayOf(CommandHandler::class.java)
        val handler = Proxy.newProxyInstance(
            javaClass.classLoader,
            interfaces,
            NoOpInvocationHandler,
        ) as CommandHandler<*>

        assertNull(commandTypes.resolveOrNull(handler::class))
    }

    @Test
    fun `the generic type itself resolves nothing`() {
        assertNull(commandTypes.resolveOrNull(CommandHandler::class))
    }

    @Test
    fun `a type variable left open resolves nothing`() {
        assertNull(commandTypes.resolveOrNull(GenericCommandHandler::class))
    }

    @Test
    fun `an array of a type variable left open resolves nothing`() {
        assertNull(commandTypes.resolveOrNull(GenericArrayCommandHandler::class))
    }

    @Test
    fun `the bound of a type variable left open is not taken`() {
        assertNull(commandTypes.resolveOrNull(BoundedCommandHandler::class))
    }

    @Test
    fun `a type variable an inner subclass passes back to its outer class resolves nothing`() {
        assertNull(commandTypes.resolveOrNull(OuterCommandHandler.InnerCommandHandler::class))
    }

    @Test
    fun `a negative index is rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            GenericTypeResolver.of(Converter::class, index = -1)
        }

        assertEquals(
            "${Converter::class.java.name} has no type parameter at index -1, its type parameters: [S, T]",
            failure.message,
        )
    }

    @Test
    fun `an index beyond the type parameters is rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            GenericTypeResolver.of(Converter::class, index = 2)
        }

        assertEquals(
            "${Converter::class.java.name} has no type parameter at index 2, its type parameters: [S, T]",
            failure.message,
        )
    }

    @Test
    fun `a type without type parameters is rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            GenericTypeResolver.of(CreateOrder::class)
        }

        assertEquals(
            "${CreateOrder::class.java.name} has no type parameter at index 0, its type parameters: []",
            failure.message,
        )
    }
}
