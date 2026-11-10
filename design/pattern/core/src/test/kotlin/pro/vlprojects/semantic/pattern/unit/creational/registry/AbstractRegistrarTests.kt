package pro.vlprojects.semantic.pattern.unit.creational.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.creational.factory.error.FactoryNotFoundException
import pro.vlprojects.semantic.pattern.creational.factory.error.InvalidDefinitionException
import pro.vlprojects.semantic.pattern.creational.factory.error.MultipleFactoriesException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceAlreadyExistsException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceRegistrationException
import pro.vlprojects.semantic.pattern.creational.registry.error.InvalidNameException

class AbstractRegistrarTests {

    private val registry = GreetingRegistry()

    @Test
    fun `every definition becomes an instance of the factory of its class`() {
        mapOf(
            "morning" to GreetingDefinition("Good morning"),
            "evening" to ShoutDefinition("Good evening"),
        )
            .let(::GreetingRegistrar)
            .populate(registry)

        assertEquals("Good morning", registry.resolve("morning"))
        assertEquals("GOOD EVENING", registry.resolve("evening"))
    }

    @Test
    fun `a wrong definition is reported with its name`() {
        val failure = assertThrows<InstanceRegistrationException> {
            mapOf("morning" to GreetingDefinition(" "))
                .let(::GreetingRegistrar)
                .populate(registry)
        }

        assertEquals("morning", failure.name)
        assertInstanceOf(InvalidDefinitionException::class.java, failure.cause)
        assertFalse("morning" in registry)
    }

    @Test
    fun `a taken name is reported with its name`() {
        registry.register("morning", "Morning")

        val failure = assertThrows<InstanceRegistrationException> {
            mapOf("morning" to GreetingDefinition("Good morning"))
                .let(::GreetingRegistrar)
                .populate(registry)
        }

        assertEquals("morning", failure.name)
        assertInstanceOf(InstanceAlreadyExistsException::class.java, failure.cause)
        assertEquals("Morning", registry.resolve("morning"))
    }

    @Test
    fun `a blank name is reported as it is`() {
        val failure = assertThrows<InstanceRegistrationException> {
            mapOf(" " to GreetingDefinition("Hello"))
                .let(::GreetingRegistrar)
                .populate(registry)
        }

        assertEquals(" ", failure.name)
        assertInstanceOf(InvalidNameException::class.java, failure.cause)
    }

    @Test
    fun `a definition without a factory is reported with its name`() {
        val failure = assertThrows<InstanceRegistrationException> {
            mapOf("silence" to SilenceDefinition)
                .let(::GreetingRegistrar)
                .populate(registry)
        }

        assertEquals("silence", failure.name)
        assertInstanceOf(FactoryNotFoundException::class.java, failure.cause)
    }

    @Test
    fun `two factories of one definition are rejected at construction`() {
        assertThrows<MultipleFactoriesException> {
            GreetingRegistrar(
                definitions = emptyMap(),
                factories = listOf(GreetingFactory, GreetingFactory),
            )
        }
    }
}
