package pro.vlprojects.semantic.pattern.unit.structural.facade

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.structural.facade.FacadeSymbolProcessorProvider
import java.util.ServiceLoader

class FacadeSymbolProcessorProviderTests {

    @Test
    fun `ServiceLoader finds the provider`() {
        val providers = ServiceLoader
            .load(SymbolProcessorProvider::class.java)
            .map { provider -> provider::class }

        assertTrue(FacadeSymbolProcessorProvider::class in providers) { "No provider in $providers" }
    }
}
