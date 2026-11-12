package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

object NoOpInvocationHandler : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, arguments: Array<out Any>?) = Unit
}
