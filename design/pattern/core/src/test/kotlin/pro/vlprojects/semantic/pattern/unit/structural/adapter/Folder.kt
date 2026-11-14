package pro.vlprojects.semantic.pattern.unit.structural.adapter

data class Folder(val name: String, val folders: List<Folder> = emptyList())
