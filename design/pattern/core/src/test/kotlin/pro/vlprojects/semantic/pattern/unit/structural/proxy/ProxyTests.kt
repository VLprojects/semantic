package pro.vlprojects.semantic.pattern.unit.structural.proxy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.structural.proxy.ProxyFactory

class ProxyTests {

    private val image = Image("photo.png")

    private val proxy = ImageProxy(Image::class, { this.image }, 50)

    private val virtual = ProxyFactory<Graphic> { image -> ImageProxy(Image::class, image, 50) }

    private val guarded = ProxyFactory<Graphic> { image -> image().let { graphic -> GuardedImage(graphic) { true } } }

    @Test
    fun `the delegate of a virtual proxy is not loaded until a call needs it`() {
        assertFalse(this.proxy.isDelegateLoaded())
    }

    @Test
    fun `a call the proxy answers itself loads nothing`() {
        assertEquals(50, this.proxy.extent)
        assertFalse(this.proxy.isDelegateLoaded())
    }

    @Test
    fun `a call the proxy passes on loads the delegate`() {
        assertEquals("photo.png drawn", this.proxy.draw())
        assertTrue(this.proxy.isDelegateLoaded())
    }

    @Test
    fun `a virtual proxy tells the class of its delegate before it is loaded`() {
        assertEquals(Image::class, this.proxy.delegateClass)
        assertFalse(this.proxy.isDelegateLoaded())
    }

    @Test
    fun `a protection proxy passes a call it allows`() {
        assertEquals("photo.png drawn", GuardedImage(this.image) { true }.draw())
    }

    @Test
    fun `a protection proxy holds back a call it does not allow`() {
        assertThrows<SecurityException> { GuardedImage(this.image) { false }.draw() }
    }

    @Test
    fun `a protection proxy forwards what it does not check`() {
        assertEquals(100, GuardedImage(this.image) { false }.extent)
    }

    @Test
    fun `a protection proxy tells the class of its delegate`() {
        assertEquals(Image::class, GuardedImage(this.image) { false }.delegateClass)
    }

    @Test
    fun `the delegate of a protection proxy is loaded from the start`() {
        assertTrue(GuardedImage(this.image) { false }.isDelegateLoaded())
    }

    @Test
    fun `a factory loads the delegate once when a call needs it`() {
        var loads = 0
        val proxy = this.virtual.proxy {
            loads++
            this.image
        }

        assertEquals(0, loads)
        assertEquals("photo.png drawn", proxy.draw())
        assertEquals("photo.png drawn", proxy.draw())
        assertEquals(1, loads)
    }

    @Test
    fun `a factory of a protection proxy takes the delegate at once`() {
        var loads = 0
        this.guarded.proxy {
            loads++
            this.image
        }

        assertEquals(1, loads)
    }
}
