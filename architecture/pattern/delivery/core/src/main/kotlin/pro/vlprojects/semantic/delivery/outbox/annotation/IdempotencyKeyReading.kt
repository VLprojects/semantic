package pro.vlprojects.semantic.delivery.outbox.annotation

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method

internal fun Any.readIdempotencyKey(): String? = this.javaClass
    .listHierarchy()
    .flatMap(Class<*>::listMembers)
    .firstOrNull { member -> member.isAnnotationPresent(IdempotencyKey::class.java) }
    ?.let { member -> this.readMember(member) }
    ?.toString()

private fun Class<*>.listHierarchy(): List<Class<*>> = this.interfaces
    .toList()
    .plus(listOfNotNull(this.superclass))
    .flatMap(Class<*>::listHierarchy)
    .plus(this)

private fun Class<*>.listMembers(): List<AccessibleObject> = this.declaredFields
    .toList<AccessibleObject>()
    .plus(this.declaredMethods)

private fun Any.readMember(member: AccessibleObject): Any? = member
    .apply { isAccessible = true }
    .let { accessible ->
        when (accessible) {
            is Field -> accessible.get(this)
            is Method -> accessible.invoke(this)
            else -> null
        }
    }
