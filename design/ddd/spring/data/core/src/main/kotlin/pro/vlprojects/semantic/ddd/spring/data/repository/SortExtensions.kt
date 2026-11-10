package pro.vlprojects.semantic.ddd.spring.data.repository

import org.springframework.data.core.toDotPath
import org.springframework.data.domain.Sort
import kotlin.reflect.KProperty1

public fun KProperty1<*, *>.sort(): Sort = this.toDotPath().let(Sort::by)

public fun KProperty1<*, *>.order(): Sort.Order = this.toDotPath().let(Sort.Order::by)
