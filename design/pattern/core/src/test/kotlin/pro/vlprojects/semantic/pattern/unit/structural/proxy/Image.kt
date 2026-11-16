package pro.vlprojects.semantic.pattern.unit.structural.proxy

class Image(private val fileName: String) : Graphic {

    override val extent = 100

    override fun draw() = "${this.fileName} drawn"
}
