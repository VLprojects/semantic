package pro.vlprojects.semantic.pattern.unit.structural.adapter

import pro.vlprojects.semantic.pattern.structural.adapter.Adapter
import pro.vlprojects.semantic.pattern.structural.composite.Composite

class FolderComposite(folder: Folder) :
    Adapter<Folder>(folder),
    Composite<FolderComposite> {

    override val children
        get() = this.adaptee.folders.map(::FolderComposite)
}
