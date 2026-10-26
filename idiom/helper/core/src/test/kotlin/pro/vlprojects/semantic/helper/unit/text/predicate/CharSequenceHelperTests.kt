package pro.vlprojects.semantic.helper.unit.text.predicate

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.predicate.hasFewerSymbolsThan

class CharSequenceHelperTests {

    @Test
    fun `fewer symbols than the count are counted as a reader sees them`() {
        assertTrue("🇷🇺👍🏽".hasFewerSymbolsThan(3))
        assertFalse("🇷🇺👍🏽".hasFewerSymbolsThan(2))
    }
}
