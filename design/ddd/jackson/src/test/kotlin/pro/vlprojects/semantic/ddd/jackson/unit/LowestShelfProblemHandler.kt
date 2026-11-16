package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.DeserializationProblemHandler

class LowestShelfProblemHandler : DeserializationProblemHandler() {

    override fun handleWeirdKey(
        context: DeserializationContext,
        rawKeyType: Class<*>,
        keyValue: String,
        failureMsg: String,
    ) = when (rawKeyType) {
        Short::class.javaObjectType -> LOWEST_SHELF
        else -> NOT_HANDLED
    }

    companion object {

        const val LOWEST_SHELF: Short = 1
    }
}
