package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import kotlin.reflect.KClass

data class BoundDelay(
    val name: String,
    val type: KClass<*>,
    val sources: List<Map<String, String>>,
)
