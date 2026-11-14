package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VisitorTests {

    private val computer = Chassis(listOf(Chassis(listOf(FloppyDisk())), FloppyDisk(), Card()))

    @Test
    fun `an element calls the visit method of its class`() {
        assertEquals(listOf("floppy disk"), FloppyDisk().accept(InventoryVisitor()))
    }

    @Test
    fun `a visit method the visitor does not override falls back to visit`() {
        assertEquals(5, FloppyDisk().accept(PricingVisitor()))
    }

    @Test
    fun `an element the hierarchy has no visit method for falls back to visit`() {
        assertEquals(30, Card().accept(PricingVisitor()))
    }

    @Test
    fun `a visitor of the base type falls back to visit`() {
        assertEquals("equipment", FloppyDisk().accept(LabelVisitor()))
    }

    @Test
    fun `a visitor adds an operation over every level`() {
        assertEquals(80, computer.accept(PricingVisitor()))
    }
}
