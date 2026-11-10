package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.factory.Factory

abstract class TextFactory<D : Factory.Definition> : Factory<D, String>
