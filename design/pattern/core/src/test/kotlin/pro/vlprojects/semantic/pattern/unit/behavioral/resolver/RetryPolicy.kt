package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import kotlin.reflect.KClass

class RetryPolicy(val failureType: KClass<out Throwable>)
