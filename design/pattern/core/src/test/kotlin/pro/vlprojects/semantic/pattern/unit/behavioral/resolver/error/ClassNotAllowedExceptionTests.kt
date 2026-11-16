package pro.vlprojects.semantic.pattern.unit.behavioral.resolver.error

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.ClassNotAllowedException

class ClassNotAllowedExceptionTests {

    @Test
    fun `the allowed packages are its own unmodifiable copy`() {
        val packages = mutableSetOf("com.acme", "org.acme")
        val failure = ClassNotAllowedException("java.lang.Runtime", packages)

        packages += "java.lang"

        assertEquals(setOf("com.acme", "org.acme"), failure.allowedPackages)
        assertThrows<UnsupportedOperationException> { (failure.allowedPackages as MutableSet<String>).add("java.lang") }
    }
}
